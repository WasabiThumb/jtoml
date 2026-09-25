/*
 * Copyright 2026 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wasabithumb.jtoml.value.table;

import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.IntFunction;

@ApiStatus.Internal
final class TomlTableBranch implements TomlTableNode {

    @Contract("_ -> new")
    public static @NotNull TomlTableBranch copyOf(@NotNull TomlTableBranch branch) {
        return copyOf(branch, null);
    }

    @Contract("_, _ -> new")
    private static @NotNull TomlTableBranch copyOf(@NotNull TomlTableBranch branch, @Nullable TomlTableBranch parent) {
        TomlTableBranch ret = new TomlTableBranch(branch.capacity);
        ret.len = branch.len;
        ret.entryCount = branch.entryCount;
        System.arraycopy(branch.labels, 0, ret.labels, 0, branch.len);

        TomlTableNode next;
        for (int i=0; i < branch.len; i++) {
            next = branch.nodes[i];
            if (next.isBranch()) {
                next = copyOf(next.asBranch(), ret);
            } else {
                TomlValue tv = next.asLeaf().value();
                next = new TomlTableLeaf(TomlValue.copyOf(tv));
            }
            ret.nodes[i] = next;
        }

        if (parent != null)
            ret.parents.add(parent);

        return ret;
    }

    private static <T> @UnknownNullability T binarySearch(
            @NotNull String needle,
            @NotNull String @NotNull [] haystack,
            int len,
            @NotNull IntFunction<T> hit,
            @NotNull IntFunction<T> miss
    ) {
        int off = 0;
        while (len > 0) {
            int hl = len >>> 1;
            int s = off + hl;
            int cmp = needle.compareTo(haystack[s]);
            if (cmp == 0) return hit.apply(s);
            if (cmp < 0) {
                len = hl;
            } else {
                off += hl + 1;
                len -= hl + 1;
            }
        }
        return miss.apply(off);
    }

    private static int grow(int n) {
        if (n == 0x7FFFFFFF) throw new OutOfMemoryError("Cannot grow array past " + n + " elements");
        if (n >= 0x40000000) return 0x7FFFFFFF;
        return n << 1;
    }

    //

    private final Set<TomlTableBranch> parents;
    private int capacity;
    private int len;
    private String[] labels;
    private TomlTableNode[] nodes;
    private int entryCount;
    TomlValue attachedValue;

    private TomlTableBranch(int capacity) {
        this.parents = Collections.newSetFromMap(new WeakHashMap<>());
        this.capacity = capacity;
        this.len = 0;
        this.labels = new String[capacity];
        this.nodes = new TomlTableNode[capacity];
        this.entryCount = 0;
        this.attachedValue = null;
    }

    TomlTableBranch() {
        this(8);
    }

    //

    /** @implNote This is a shallow listing */
    public @NotNull @Unmodifiable List<String> keys() {
        return Collections.unmodifiableList(Arrays.asList(this.labels).subList(0, this.len));
    }

    /** @implNote This is a shallow count */
    public int keyCount() {
        return this.len;
    }

    public void clear() {
        this.len = 0;
        if (this.capacity > 8) this.resize(8);
        this.modifyEntryCount(-this.entryCount);
    }

    public @Nullable TomlTableNode get(@NotNull String label) {
        return binarySearch(
                label,
                this.labels,
                this.len,
                (int i) -> this.nodes[i],
                (int ignored) -> null
        );
    }

    public @Nullable TomlTableNode put(@NotNull String label, @NotNull TomlTableNode node) {
        return binarySearch(
                label,
                this.labels,
                this.len,
                (int i) -> {
                    // clobber
                    TomlTableNode old = this.nodes[i];
                    this.nodes[i] = node;
                    if (old.isBranch()) this.tryUnparent(old.asBranch());
                    if (node.isBranch()) node.asBranch().addParent(this);
                    this.modifyEntryCount(node.entryCount() - old.entryCount());
                    return old;
                },
                (int i) -> {
                    // insert
                    this.ensureSpace();
                    System.arraycopy(this.nodes, i, this.nodes, i + 1, this.len - i);
                    System.arraycopy(this.labels, i, this.labels, i + 1, this.len - i);
                    this.nodes[i] = node;
                    this.labels[i] = label;
                    this.len++;
                    if (node.isBranch()) node.asBranch().addParent(this);
                    this.modifyEntryCount(node.entryCount());
                    return null;
                }
        );
    }

    public @Nullable TomlTableNode remove(@NotNull String label) {
        return binarySearch(
                label,
                this.labels,
                this.len,
                (int i) -> {
                    TomlTableNode node = this.nodes[i];
                    this.len--;
                    this.modifyEntryCount(-node.entryCount());
                    System.arraycopy(this.nodes, i + 1, this.nodes, i, this.len - i);
                    System.arraycopy(this.labels, i + 1, this.labels, i, this.len - i);
                    if (node.isBranch()) this.tryUnparent(node.asBranch());
                    this.tryShrink();
                    return node;
                },
                (int ignored) -> null
        );
    }

    private void modifyEntryCount(int mod) {
        this.entryCount += mod;
        for (TomlTableBranch parent : this.parents)
            parent.modifyEntryCount(mod);
    }

    private boolean isInHierarchy(@NotNull TomlTableBranch subject) {
        if (this.equals(subject)) return true;
        for (TomlTableBranch parent : this.parents) {
            if (parent.isInHierarchy(subject)) return true;
        }
        return false;
    }

    private void addParent(@NotNull TomlTableBranch parent) {
        if (parent.isInHierarchy(this))
            throw new IllegalStateException("Attempt to create circular table relationship");
        this.parents.add(parent);
    }

    private void removeParent(@NotNull TomlTableBranch parent) {
        this.parents.remove(parent);
    }

    private void tryUnparent(@NotNull TomlTableBranch child) {
        for (int i = 0; i < this.len; i++) {
            if (child.sameIdentity(this.nodes[i])) return;
        }
        child.removeParent(this);
    }

    private void resize(int tc) {
        TomlTableNode[] nn = new TomlTableNode[tc];
        System.arraycopy(this.nodes, 0, nn, 0, this.len);

        String[] nl = new String[tc];
        System.arraycopy(this.labels, 0, nl, 0, this.len);

        this.capacity = tc;
        this.nodes = nn;
        this.labels = nl;
    }

    private void ensureSpace() {
        int cap = this.capacity;
        if (this.len < cap) return;
        this.resize(grow(cap));
    }

    private void tryShrink() {
        int tc = this.capacity >> 1;
        if (this.len > tc) return;
        this.resize(tc);
    }

    private boolean sameIdentity(Object other) {
        return super.equals(other);
    }

    // START Node Super

    @Override
    public int entryCount() {
        return this.entryCount;
    }

    @Override
    public boolean isBranch() {
        return true;
    }

    @Override
    @Contract("-> this")
    public @NotNull TomlTableBranch asBranch() {
        return this;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    @Contract("-> fail")
    public @NotNull TomlTableLeaf asLeaf() {
        throw new UnsupportedOperationException();
    }

    // END Node Super

    @Override
    public int hashCode() {
        int h = 7;
        for (int i = 0; i < this.len; i++) {
            h = 31 * h + this.labels[i].hashCode();
            h = 31 * h + this.nodes[i].hashCode();
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TomlTableBranch)) return false;
        int len = this.len;
        TomlTableBranch other = (TomlTableBranch) obj;
        if (len != other.len) return false;
        for (int i = 0; i < len; i++) {
            if (!Objects.equals(this.labels[i], other.labels[i])) return false;
            if (!Objects.equals(this.nodes[i], other.nodes[i])) return false;
        }
        return true;
    }

}
