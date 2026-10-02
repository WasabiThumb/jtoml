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

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
final class TomlTableImpl implements TomlTable {

    static TomlTableImpl copyOf(TomlTable table) {
        if (table instanceof TomlTableImpl) {
            TomlTableImpl qual = (TomlTableImpl) table;
            return new TomlTableImpl(
                    TomlTableBranch.copyOf(qual.root),
                    Comments.copyOf(qual.comments)
            );
        } else {
            TomlTableImpl ret = new TomlTableImpl(
                    new TomlTableBranch(),
                    Comments.copyOf(table.comments())
            );
            for (TomlTable.Entry<?> entry : table.entries()) {
                ret.put(entry.key(), entry.value());
            }
            return ret;
        }
    }

    private static <V extends TomlValue> TomlTable.Entry<V> entryOf(
            TomlKey key,
            V value
    ) {
        return new EntryImpl<>(key, value);
    }

    //

    private final long creationTime;
    private final TomlTableBranch root;
    private final Comments comments;
    private transient byte flags;

    private TomlTableImpl(TomlTableBranch root, Comments comments) {
        this.creationTime = System.nanoTime();
        this.root = root;
        this.comments = comments;
        this.flags = 0;
    }

    private TomlTableImpl(TomlTableBranch root) {
        this(root, Comments.empty());
    }

    TomlTableImpl() {
        this(new TomlTableBranch());
    }

    //
    
    @Override
    public long creationTime() {
        return this.creationTime;
    }

    @Override
    public int flags() {
        return this.flags & 0xFF;
    }

    @Override
    public TomlTable flags(int flags) {
        this.flags = (byte) flags;
        return this;
    }

    @Override
    public Comments comments() {
        return this.comments;
    }

    @Override
    public int size() {
        return this.root.entryCount();
    }

    @Override
    public boolean isEmpty() {
        return this.root.entryCount() == 0;
    }

    @Override
    public void clear() {
        this.root.clear();
    }

    @Override
    public Set<TomlKey> keys(boolean deep) {
        return deep ?
                new DeepKeySet(this) :
                new ShallowKeySet(this.root);
    }

    @Override
    public Set<Entry<?>> entries(boolean deep) {
        return deep ?
                new DeepEntrySet(this) :
                new ShallowEntrySet(this.root);
    }

    @Override
    public boolean contains(TomlKey key) {
        Resolution r = this.resolve(key, false);
        if (r == null) return false;
        return r.branch.get(r.label) != null;
    }

    @Override
    public @Nullable TomlValue get(TomlKey key) {
        Resolution r = this.resolve(key, false);
        if (r == null) return null;
        TomlTableNode node = r.branch.get(r.label);
        return this.wrapNode(node);
    }

    @Override
    public @Nullable TomlValue put(TomlKey key, TomlValue value) {
        Resolution r = this.resolve(key, true);
        TomlTableNode old;
        if (value.isTable()) {
            TomlTableImpl tbl = (TomlTableImpl) value.asTable();
            tbl.root.attachedValue = value;
            old = r.branch.put(r.label, tbl.root);
        } else {
            TomlTableLeaf leaf = new TomlTableLeaf(value);
            old = r.branch.put(r.label, leaf);
        }
        return this.wrapNode(old);
    }

    @Override
    public @Nullable TomlValue remove(TomlKey key) {
        Resolution r = this.resolve(key, false);
        if (r == null) return null;
        TomlTableNode node = r.branch.remove(r.label);
        return this.wrapNode(node);
    }

    @Contract("null -> null; !null -> !null")
    private @Nullable TomlValue wrapNode(@Nullable TomlTableNode node) {
        if (node == null) return null;
        if (node.isLeaf()) {
            return node.asLeaf().value();
        } else {
            TomlTableBranch branch = node.asBranch();
            TomlValue ret = branch.attachedValue;
            if (ret == null) {
                ret = new TomlTableImpl(branch);
                branch.attachedValue = ret;
            }
            return ret;
        }
    }

    @Contract("_, true -> !null")
    private @Nullable Resolution resolve(TomlKey key, boolean create) {
        Objects.requireNonNull(key, "key must not be null");
        Iterator<String> iter = key.iterator();
        if (!iter.hasNext()) throw new IllegalArgumentException("Cannot use empty (zero part) key in TomlTable");

        TomlTableBranch head = this.root;
        String label = iter.next();

        while (iter.hasNext()) {
            TomlTableNode node = head.get(label);
            if (node != null && node.isBranch()) {
                head = node.asBranch();
            } else if (create) {
                TomlTableBranch branch = new TomlTableBranch();
                head.put(label, branch);
                head = branch;
            } else {
                return null;
            }
            label = iter.next();
        }

        return new Resolution(head, label);
    }

    @Override
    public int hashCode() {
        return this.root.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TomlTableImpl &&
                this.root.equals(((TomlTableImpl) obj).root);
    }

    @Override
    public String toString() {
        Iterator<TomlKey> iter = this.keys(true).iterator();
        if (!iter.hasNext()) return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (true) {
            TomlKey next = iter.next();
            TomlValue value = this.get(next);
            if (value == null) throw new ConcurrentModificationException();
            sb.append(next);
            sb.append('=');
            sb.append(value);
            if (!iter.hasNext()) break;
            sb.append(", ");
        }

        return sb.append('}')
                .toString();
    }

    //

    private static final class Resolution {

        final TomlTableBranch branch;
        final String label;

        Resolution(
                TomlTableBranch branch,
                String label
        ) {
            this.branch = branch;
            this.label = label;
        }

    }

    private static abstract class ShallowIterator<T> implements Iterator<T> {

        protected final TomlTableBranch parent;
        private final Iterator<String> backing;

        ShallowIterator(TomlTableBranch parent) {
            this.parent = parent;
            this.backing = parent.keys().iterator();
        }

        //

        @Override
        public boolean hasNext() {
            return this.backing.hasNext();
        }

        @Override
        public T next() {
            return this.adapt(this.backing.next());
        }

        @Override
        public void remove() {
            this.backing.remove();
        }

        protected abstract T adapt(String key);

        //

        static final class OfKeys extends ShallowIterator<TomlKey> {

            OfKeys(TomlTableBranch parent) {
                super(parent);
            }

            @Override
            protected TomlKey adapt(String key) {
                return TomlKey.literal(key);
            }

        }

        static final class OfEntries extends ShallowIterator<TomlTable.Entry<?>> {

            OfEntries(TomlTableBranch parent) {
                super(parent);
            }

            @Override
            protected Entry<?> adapt(String key) {
                TomlTableNode node = this.parent.get(key);
                if (node == null) throw new ConcurrentModificationException();
                TomlValue value;
                if (node.isLeaf()) {
                    value = node.asLeaf().value();
                } else {
                    TomlTableBranch branch = node.asBranch();
                    TomlValue attached = branch.attachedValue;
                    if (attached != null) {
                        value = attached;
                    } else {
                        value = new TomlTableImpl(branch);
                        branch.attachedValue = value;
                    }
                }
                return entryOf(TomlKey.literal(key), value);
            }

        }

    }

    private static final class ShallowKeySet extends AbstractSet<TomlKey> {

        private final TomlTableBranch parent;

        ShallowKeySet(TomlTableBranch parent) {
            this.parent = parent;
        }

        //

        @Override
        public int size() {
            return this.parent.keyCount();
        }

        @Override
        public Iterator<TomlKey> iterator() {
            return new ShallowIterator.OfKeys(this.parent);
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof TomlKey)) return false;
            TomlKey key = (TomlKey) o;
            if (key.size() != 1) return false;
            return this.parent.get(key.get(0)) != null;
        }

        @Override
        public void clear() {
            this.parent.clear();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof TomlKey)) return false;
            TomlKey key = (TomlKey) o;
            if (key.size() != 1) return false;
            return this.parent.remove(key.get(0)) != null;
        }

    }

    private static final class ShallowEntrySet extends AbstractSet<TomlTable.Entry<?>> {

        private final TomlTableBranch parent;

        ShallowEntrySet(TomlTableBranch parent) {
            this.parent = parent;
        }

        //

        @Override
        public int size() {
            return this.parent.keyCount();
        }

        @Override
        public Iterator<Entry<?>> iterator() {
            return new ShallowIterator.OfEntries(this.parent);
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof TomlTable.Entry<?>)) return false;
            TomlTable.Entry<?> entry = (TomlTable.Entry<?>) o;
            TomlKey key = entry.key();
            if (key.size() != 1) return false;
            TomlTableNode node = this.parent.get(key.get(0));
            if (node == null) return false;
            if (node.isLeaf()) return Objects.equals(entry.value(), node.asLeaf().value());
            return Objects.equals(entry.value(), node.asBranch().attachedValue);
        }

        @Override
        public void clear() {
            this.parent.clear();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof TomlTable.Entry<?>)) return false;
            TomlTable.Entry<?> entry = (TomlTable.Entry<?>) o;
            TomlKey key = entry.key();
            if (key.size() != 1) return false;
            TomlTableNode node = this.parent.get(key.get(0));
            if (node == null) return false;
            if (!Objects.equals(
                    entry.value(),
                    node.isLeaf() ?
                            node.asLeaf().value() :
                            node.asBranch().attachedValue
            )) return false;
            this.parent.remove(key.get(0));
            return true;
        }

    }

    private static abstract class DeepIterator<T> implements Iterator<T> {

        private final Queue<Sub<T>> queue;

        DeepIterator(TomlTableImpl parent) {
            this.queue = new ArrayDeque<>();
            this.queue.add(new Sub<>(this, TomlKey.literal(), parent.root));
        }

        //

        private @Nullable Sub<T> acquire() {
            Sub<T> ret = this.queue.peek();
            while (ret != null) {
                if (ret.hasNext()) return ret;
                this.queue.poll();
                ret = this.queue.peek();
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            for (Sub<T> sub : this.queue) {
                if (sub.hasNext()) return true;
            }
            return false;
        }

        @Override
        public T next() {
            Sub<T> sub = this.acquire();
            if (sub == null) throw new NoSuchElementException();
            return sub.next();
        }

        @Override
        public void remove() {
            Sub<T> sub = this.queue.peek();
            if (sub == null) throw new NoSuchElementException();
            sub.remove();
        }

        protected abstract T adapt(TomlKey key, TomlValue value);

        //

        private static final class Sub<T> implements Iterator<T> {

            private final DeepIterator<T> parent;
            private final TomlKey prefix;
            private final TomlTableBranch branch;
            private final Iterator<String> backing;
            private volatile boolean churnOk;
            private @UnknownNullability String churnLabel;
            private @UnknownNullability TomlValue churnValue;

            Sub(
                    DeepIterator<T> parent,
                    TomlKey prefix,
                    TomlTableBranch branch
            ) {
                this.parent = parent;
                this.prefix = prefix;
                this.branch = branch;
                this.backing = branch.keys().iterator();
            }

            //

            @Contract(mutates = "this")
            private void churn() {
                if (this.churnOk) return;

                String label;
                TomlTableNode node;

                while (this.backing.hasNext()) {
                    label = this.backing.next();
                    node = this.branch.get(label);
                    if (node == null) continue;
                    if (node.isBranch()) {
                        this.parent.queue.add(new Sub<>(
                                this.parent,
                                TomlKey.join(this.prefix, TomlKey.literal(label)),
                                node.asBranch()
                        ));
                        continue;
                    }
                    this.churnOk = true;
                    this.churnLabel = label;
                    this.churnValue = node.asLeaf().value();
                    break;
                }
            }

            @Override
            public boolean hasNext() {
                this.churn();
                return this.churnOk;
            }

            @Override
            public T next() {
                this.churn();
                if (!this.churnOk) throw new NoSuchElementException();
                this.churnOk = false;
                return this.parent.adapt(
                        TomlKey.join(this.prefix, TomlKey.literal(this.churnLabel)),
                        this.churnValue
                );
            }

            @Override
            public void remove() {
                this.backing.remove();
            }

        }

        static final class OfKeys extends DeepIterator<TomlKey> {

            OfKeys(TomlTableImpl parent) {
                super(parent);
            }

            @Override
            protected TomlKey adapt(TomlKey key, TomlValue ignored) {
                return key;
            }

        }

        static final class OfEntries extends DeepIterator<TomlTable.Entry<?>> {

            OfEntries(TomlTableImpl parent) {
                super(parent);
            }

            @Override
            protected Entry<?> adapt(TomlKey key, TomlValue value) {
                return entryOf(key, value);
            }

        }

    }

    private static final class DeepKeySet extends AbstractSet<TomlKey> {

        private final TomlTableImpl parent;

        DeepKeySet(TomlTableImpl parent) {
            this.parent = parent;
        }

        //

        @Override
        public int size() {
            return this.parent.size();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof TomlKey)) return false;
            return this.parent.contains((TomlKey) o);
        }

        @Override
        public Iterator<TomlKey> iterator() {
            return new DeepIterator.OfKeys(this.parent);
        }

    }

    private static final class DeepEntrySet extends AbstractSet<TomlTable.Entry<?>> {

        private final TomlTableImpl parent;

        DeepEntrySet(TomlTableImpl parent) {
            this.parent = parent;
        }

        //

        @Override
        public int size() {
            return this.parent.size();
        }

        @Override
        public Iterator<Entry<?>> iterator() {
            return new DeepIterator.OfEntries(this.parent);
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof TomlTable.Entry<?>)) return false;
            TomlTable.Entry<?> entry = (TomlTable.Entry<?>) o;
            return Objects.equals(entry.value(), this.parent.get(entry.key()));
        }

    }

    private static final class EntryImpl<V extends TomlValue> implements TomlTable.Entry<V> {

        private final TomlKey key;
        private final V value;

        EntryImpl(
                TomlKey key,
                V value
        ) {
            this.key = key;
            this.value = value;
        }

        //

        @Override
        public TomlKey key() {
            return this.key;
        }

        @Override
        public V value() {
            return this.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.key, this.value);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TomlTable.Entry<?>)) return false;
            TomlTable.Entry<?> other = (TomlTable.Entry<?>) obj;
            return this.key.equals(other.key()) &&
                    this.value.equals(other.value());
        }

        @Override
        public String toString() {
            return "Entry{key=" + this.key +
                    ", value=" + this.value +
                    "}";
        }

    }

}
