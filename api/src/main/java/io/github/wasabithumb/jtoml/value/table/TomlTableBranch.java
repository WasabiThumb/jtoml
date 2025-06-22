package io.github.wasabithumb.jtoml.value.table;

import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import org.jetbrains.annotations.*;

import java.util.*;

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
        TomlTableNode next;
        int cmp;
        for (int i=0; i < this.len; i++) {
            next = this.nodes[i];
            cmp = label.compareTo(this.labels[i]);
            if (cmp < 0) {
                break;
            } else if (cmp == 0) {
                return next;
            }
        }
        return null;
    }

    public @Nullable TomlTableNode put(@NotNull String label, @NotNull TomlTableNode node) {
        int idx = this.len;
        boolean shift = false;

        if (node.isBranch())
            node.asBranch().addParent(this);

        TomlTableNode next;
        int cmp;
        for (int i=0; i < this.len; i++) {
            next = this.nodes[i];
            cmp = label.compareTo(this.labels[i]);
            if (cmp == 0) {
                // clobber
                this.nodes[i] = node;
                this.modifyEntryCount(node.entryCount() - next.entryCount());
                return next;
            } else if (cmp < 0) {
                // insert
                idx = i;
                shift = true;
                break;
            }
        }

        this.ensureSpace();
        if (shift) {
            System.arraycopy(this.nodes, idx, this.nodes, idx + 1, this.len - idx);
            System.arraycopy(this.labels, idx, this.labels, idx + 1, this.len - idx);
        }
        this.nodes[idx] = node;
        this.labels[idx] = label;
        this.len++;
        this.modifyEntryCount(node.entryCount());
        return null;
    }

    public @Nullable TomlTableNode remove(@NotNull String label) {
        TomlTableNode next;
        int cmp;
        for (int i=0; i < this.len; i++) {
            next = this.nodes[i];
            cmp = label.compareTo(this.labels[i]);
            if (cmp != 0) {
                if (cmp > 0) break;
                continue;
            }
            if (next.isBranch())
                next.asBranch().removeParent(this);
            this.len--;
            this.modifyEntryCount(-next.entryCount());
            System.arraycopy(this.nodes, i + 1, this.nodes, i, this.len - i);
            System.arraycopy(this.labels, i + 1, this.labels, i, this.len - i);
            this.tryShrink();
            return next;
        }
        return null;
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
        if (this.len < this.capacity) return;
        this.resize(this.capacity << 1);
    }

    private void tryShrink() {
        int tc = this.capacity >> 1;
        if (this.len > tc) return;
        this.resize(tc);
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

}
