package io.github.wasabithumb.jtoml.value.table;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.*;

import java.util.*;

@ApiStatus.Internal
final class TomlTableImpl implements TomlTable {

    private static @NotNull TomlTableImpl unwrapTable(@NotNull TomlValue value) {
        return (TomlTableImpl) value.asTable();
    }

    //

    private final Set<TomlTableImpl> parents = Collections.newSetFromMap(new WeakHashMap<>());
    private final Map<String, TomlValue> backing = new HashMap<>();
    private int entryCount = 0;

    TomlTableImpl() { }

    //

    @Override
    public int size() {
        return this.entryCount;
    }

    @Override
    public boolean isEmpty() {
        return this.entryCount == 0;
    }

    @Override
    public void clear() {
        this.backing.clear();
        this.entryCount = 0;
    }

    @Override
    public @NotNull @Unmodifiable Set<TomlKey> keys(boolean deep) {
        if (deep) {
            return new DeepKeySet(this);
        } else {
            return new ShallowKeySet(this.backing.keySet());
        }
    }

    @Override
    public boolean contains(@NotNull TomlKey key) {
        this.checkValidKey(key);
        TomlTableImpl head;
        TomlValue next = this;

        for (String part : key) {
            if (!next.isTable()) return false;
            head = unwrapTable(next);
            next = head.backing.get(part);
            if (next == null) return false;
        }

        return true;
    }

    @Override
    public @Nullable TomlValue get(@NotNull TomlKey key) {
        this.checkValidKey(key);
        TomlTableImpl head;
        TomlValue next = this;

        for (String part : key) {
            if (!next.isTable()) return null;
            head = unwrapTable(next);
            next = head.backing.get(part);
            if (next == null) return null;
        }

        return next;
    }

    @Override
    public @Nullable TomlValue put(@NotNull TomlKey key, @NotNull TomlValue value) {
        this.checkValidKey(key);
        this.checkValidValue(value);
        TomlTableImpl head = this;

        Iterator<String> iter = key.iterator();
        String part;
        TomlValue v;
        do {
            part = iter.next();
            if (!iter.hasNext()) break;
            v = head.backing.get(part);
            if (v == null) {
                TomlTableImpl n = new TomlTableImpl();
                n.parents.addAll(this.parents);
                n.parents.add(head);
                head.backing.put(part, n);
                v = n;
            } else if (!v.isTable()) {
                throw new IllegalArgumentException("Creating entry at " + key +
                        " would overwrite an existing non-table entry (" + part + ")");
            }
            head = unwrapTable(v);
        } while (true);

        TomlValue removed = head.backing.put(part, value);
        int mod = -this.entryCountOf(removed);
        if (value.isTable()) {
            TomlTableImpl addedTable = unwrapTable(value);
            addedTable.parents.addAll(this.parents);
            addedTable.parents.add(head);
            mod += addedTable.entryCount;
        } else {
            mod++;
        }
        this.modifyEntryCount(mod);
        return removed;
    }

    @Override
    public @Nullable TomlValue remove(@NotNull TomlKey key) {
        this.checkValidKey(key);
        TomlTableImpl head = this;

        Iterator<String> iter = key.iterator();
        String part;
        TomlValue v;
        do {
            part = iter.next();
            if (!iter.hasNext()) break;
            v = head.backing.get(part);
            if (v == null || !v.isTable()) return null;
            head = unwrapTable(v);
        } while (true);

        TomlValue removed = head.backing.remove(part);
        if (removed != null){
            if (removed.isTable()) {
                TomlTableImpl removedTable = unwrapTable(removed);
                removedTable.parents.removeAll(this.parents);
                removedTable.parents.remove(this);
                this.modifyEntryCount(-removedTable.entryCount);
            } else {
                this.modifyEntryCount(-1);
            }
        }
        return removed;
    }

    public void putAll(@NotNull TomlTableImpl other) {
        this.putAll0(TomlKey.literal(), other.backing);
    }

    private void putAll0(@NotNull TomlKey prefix, @NotNull Map<String, TomlValue> map) {
        TomlKey key;
        TomlValue value;
        for (Map.Entry<String, TomlValue> entry : map.entrySet()) {
            key = TomlKey.join(prefix, TomlKey.literal(entry.getKey()));
            value = entry.getValue();
            if (value.isTable()) {
                this.putAll0(key, unwrapTable(value).backing);
            } else {
                this.put(key, value);
            }
        }
    }

    @Contract("null -> fail")
    private void checkValidKey(TomlKey key) {
        if (key == null) throw new NullPointerException("Key may not be null");
        if (key.isEmpty()) throw new IllegalArgumentException("Key is empty (0 parts)");
    }

    private int entryCountOf(@Nullable TomlValue value) {
        if (value == null) return 0;
        if (value.isTable()) return unwrapTable(value).entryCount;
        return 1;
    }

    private void modifyEntryCount(int mod) {
        this.entryCount += mod;
        for (TomlTableImpl parent : this.parents) parent.entryCount += mod;
    }

    @Contract("null -> fail")
    private void checkValidValue(TomlValue value) {
        if (value == null) throw new NullPointerException("Value may not be null");
        if (value.isTable()) {
            if (this.equals(value)) {
                throw new IllegalArgumentException("Cannot add table to itself");
            }
            TomlTableImpl table = unwrapTable(value);
            if (this.isInAncestry(table) || table.isInAncestry(this)) {
                throw new IllegalArgumentException("Cannot create cyclic table relationship");
            }
        }
    }

    private boolean isInAncestry(@NotNull TomlTableImpl value) {
        return this.parents.contains(value);
    }

    //

    private static final class ShallowKeySet extends AbstractSet<TomlKey> {

        private final Set<String> backing;

        ShallowKeySet(@NotNull Set<String> backing) {
            this.backing = backing;
        }

        //

        @Override
        public int size() {
            return this.backing.size();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof TomlKey)) return false;
            TomlKey key = (TomlKey) o;
            if (key.size() != 1) return false;
            return this.backing.contains(key.get(0));
        }

        @Override
        public @NotNull Iterator<TomlKey> iterator() {
            return new Iter(this.backing.iterator());
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
            return this.parent.entryCount;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof TomlKey)) return false;
            return this.parent.contains((TomlKey) o);
        }

        @Override
        public @NotNull Iterator<TomlKey> iterator() {
            return new Iter(this.parent);
        }

        //

        private static final class Iter implements Iterator<TomlKey> {

            private final Queue<QueuedTable> queue;
            private TomlKey prefix;
            private Iterator<Map.Entry<String, TomlValue>> backing;
            private int remaining;

            Iter(@NotNull TomlTableImpl start) {
                this.queue = new LinkedList<>();
                this.prefix = TomlKey.literal();
                this.backing = start.backing.entrySet().iterator();
                this.remaining = start.entryCount;
            }

            //

            @Override
            public boolean hasNext() {
                return this.remaining > 0;
            }

            @Override
            public @NotNull TomlKey next() {
                if (this.remaining <= 0) throw new NoSuchElementException();

                while (true) {
                    while (this.backing.hasNext()) {
                        Map.Entry<String, TomlValue> entry = this.backing.next();
                        TomlKey key = TomlKey.join(this.prefix, TomlKey.literal(entry.getKey()));
                        TomlValue value = entry.getValue();
                        if (value.isTable()) {
                            this.queue.add(new QueuedTable(key, unwrapTable(value)));
                        } else {
                            this.remaining--;
                            return key;
                        }
                    }

                    QueuedTable queued = this.queue.poll();
                    if (queued == null) throw new ConcurrentModificationException();
                    this.prefix = queued.prefix;
                    this.backing = queued.table.backing.entrySet().iterator();
                }
            }

            //

            private static final class QueuedTable {

                private final TomlKey prefix;
                private final TomlTableImpl table;

                QueuedTable(@NotNull TomlKey prefix, @NotNull TomlTableImpl table) {
                    this.prefix = prefix;
                    this.table = table;
                }

            }

        }

    }

}
