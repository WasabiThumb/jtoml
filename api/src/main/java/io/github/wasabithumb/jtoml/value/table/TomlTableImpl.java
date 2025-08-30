package io.github.wasabithumb.jtoml.value.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.*;

import java.util.*;

@ApiStatus.Internal
final class TomlTableImpl implements TomlTable {

    static @NotNull TomlTableImpl copyOf(@NotNull TomlTableImpl table) {
        return new TomlTableImpl(
                TomlTableBranch.copyOf(table.root),
                Comments.copyOf(table.comments)
        );
    }

    //

    private final TomlTableBranch root;
    private final Comments comments;
    private transient byte flags;

    private TomlTableImpl(@NotNull TomlTableBranch root, @NotNull Comments comments) {
        this.root = root;
        this.comments = comments;
        this.flags = 0;
    }

    private TomlTableImpl(@NotNull TomlTableBranch root) {
        this(root, Comments.empty());
    }

    TomlTableImpl() {
        this(new TomlTableBranch());
    }

    //


    @Override
    public int flags() {
        return this.flags & 0xFF;
    }

    @Override
    public @NotNull TomlTable flags(int flags) {
        this.flags = (byte) flags;
        return this;
    }

    @Override
    public @NotNull Comments comments() {
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
    public @NotNull @Unmodifiable Set<TomlKey> keys(boolean deep) {
        return deep ?
                new DeepKeySet(this) :
                new ShallowKeySet(this.root);
    }

    @Override
    public boolean contains(@NotNull TomlKey key) {
        Resolution r = this.resolve(key, false);
        if (r == null) return false;
        return r.branch.get(r.label) != null;
    }

    @Override
    public @Nullable TomlValue get(@NotNull TomlKey key) {
        Resolution r = this.resolve(key, false);
        if (r == null) return null;
        TomlTableNode node = r.branch.get(r.label);
        return this.wrapNode(node);
    }

    @Override
    public @Nullable TomlValue put(@NotNull TomlKey key, @NotNull TomlValue value) {
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
    public @Nullable TomlValue remove(@NotNull TomlKey key) {
        Resolution r = this.resolve(key, false);
        if (r == null) return null;
        TomlTableNode node = r.branch.remove(r.label);
        return this.wrapNode(node);
    }

    @Contract("null -> null; !null -> !null")
    private TomlValue wrapNode(TomlTableNode node) {
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

    @Contract("null, _ -> fail; _, true -> !null")
    private @Nullable Resolution resolve(TomlKey key, boolean create) {
        if (key == null) throw new NullPointerException("Key may not be null");

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

    //

    private static final class Resolution {

        final TomlTableBranch branch;
        final String label;

        Resolution(
                @NotNull TomlTableBranch branch,
                @NotNull String label
        ) {
            this.branch = branch;
            this.label = label;
        }

    }

    private static final class ShallowKeySet extends AbstractSet<TomlKey> {

        private final TomlTableBranch parent;

        ShallowKeySet(@NotNull TomlTableBranch parent) {
            this.parent = parent;
        }

        //

        @Override
        public int size() {
            return this.parent.keyCount();
        }

        @Override
        public @NotNull Iter iterator() {
            return new Iter(this.parent.keys().iterator());
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof TomlKey)) return false;
            TomlKey key = (TomlKey) o;
            if (key.size() != 1) return false;
            return this.parent.get(key.get(0)) != null;
        }

        //

        private static final class Iter implements Iterator<TomlKey> {

            private final Iterator<String> backing;

            Iter(@NotNull Iterator<String> backing) {
                this.backing = backing;
            }

            //

            @Override
            public boolean hasNext() {
                return this.backing.hasNext();
            }

            @Override
            public @NotNull TomlKey next() {
                return TomlKey.literal(this.backing.next());
            }

        }

    }

    private static final class DeepKeySet extends AbstractSet<TomlKey> {

        private final TomlTableImpl parent;

        DeepKeySet(@NotNull TomlTableImpl parent) {
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
        public @NotNull Iterator<TomlKey> iterator() {
            return new Iter(this.parent.root);
        }

        //

        private static final class Iter implements Iterator<TomlKey> {

            private final Queue<SubIter> queue;

            Iter(@NotNull TomlTableBranch branch) {
                this.queue = new LinkedList<>();
                this.queue.add(new SubIter(TomlKey.literal(), branch, this.queue));
            }

            //

            private @Nullable SubIter acquire() {
                SubIter ret = this.queue.peek();
                while (ret != null) {
                    if (ret.hasNext()) return ret;
                    this.queue.poll();
                    ret = this.queue.peek();
                }
                return null;
            }

            @Override
            public boolean hasNext() {
                return this.acquire() != null;
            }

            @Override
            public @NotNull TomlKey next() {
                SubIter sub = this.acquire();
                if (sub == null) throw new NoSuchElementException();
                return sub.next();
            }

            //

            private static final class SubIter implements Iterator<TomlKey> {

                private final TomlKey prefix;
                private final TomlTableBranch branch;
                private final Iterator<String> backing;
                private final Queue<SubIter> queue;
                private TomlKey head;

                SubIter(
                        @NotNull TomlKey prefix,
                        @NotNull TomlTableBranch branch,
                        @NotNull Queue<SubIter> queue
                ) {
                    this.prefix = prefix;
                    this.branch = branch;
                    this.backing = branch.keys().iterator();
                    this.queue = queue;
                }

                //

                private void compute() {
                    if (this.head != null) return;

                    String label;
                    TomlKey key;
                    TomlTableNode node;

                    while (this.backing.hasNext()) {
                        label = this.backing.next();
                        key = TomlKey.join(this.prefix, TomlKey.literal(label));
                        node = this.branch.get(label);
                        if (node == null) throw new ConcurrentModificationException();
                        if (node.isBranch()) {
                            this.queue.add(new SubIter(key, node.asBranch(), this.queue));
                        } else if (node.isLeaf()) {
                            this.head = key;
                            break;
                        }
                    }
                }

                @Override
                public boolean hasNext() {
                    this.compute();
                    return this.head != null;
                }

                @Override
                public @NotNull TomlKey next() {
                    this.compute();
                    TomlKey ret = this.head;
                    this.head = null;
                    if (ret == null)
                        throw new NoSuchElementException();
                    return ret;
                }

            }

        }

    }

}
