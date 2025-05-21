package io.github.wasabithumb.jtoml.serial.reflect.model;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.util.RecordSupport;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@ApiStatus.Internal
public interface TableTypeModel<T> extends TypeModel<T> {

    @SuppressWarnings("unchecked")
    static <O> @Nullable TableTypeModel<O> match(@NotNull ParameterizedClass<O> pc) {
        Class<O> raw = pc.raw();

        // Record
        if (RecordSupport.isRecord(raw))
            return new Record<>(raw);

        // TomlSerializable
        if (TomlSerializable.class.isAssignableFrom(raw))
            return (TableTypeModel<O>) Serializable.create(raw);

        // TomlTable
        if (TomlTable.class.equals(raw))
            return (TableTypeModel<O>) Toml.INSTANCE;

        // Map<String, ?>
        ParameterizedClass<?> mt = pc.declaredInterface(Map.class);
        if (mt != null && mt.paramCount() >= 2 && String.class.equals(mt.param(0))) {
            ParameterizedClass<?> vt = ParameterizedClass.of(mt.param(1));
            return (TableTypeModel<O>) StringMap.create(raw.asSubclass(Map.class), vt);
        }

        // Other
        return null;
    }

    //

    @NotNull Builder<T> create();

    @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull T instance);

    @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key);

    @UnknownNullability Object get(@NotNull T instance, @NotNull TomlKey key);

    //

    interface Builder<O> {

        void set(@NotNull TomlKey key, @NotNull Object value);

        @NotNull O build();

    }

    //

    final class Serializable<T extends TomlSerializable> implements TableTypeModel<T> {

        private static Serializable<?> create(@NotNull Class<?> cls) {
            return new Serializable<>(cls.asSubclass(TomlSerializable.class));
        }

        //

        private final Class<T> type;

        private Serializable(@NotNull Class<T> type) {
            this.type = type;
        }

        //

        @Override
        public @NotNull Class<T> type() {
            return this.type;
        }

        @Override
        public @NotNull TableTypeModel.Builder<T> create() {
            Constructor<?> con;
            try {
                con = this.type.getDeclaredConstructor();
            } catch (NoSuchMethodException e1) {
                throw new IllegalStateException(
                        "No primary constructor for TomlSerializable type (" +
                                this.type.getName() + ")",
                        e1
                );
            }

            try {
                con.setAccessible(true);
            } catch (Exception ignored) { }

            Object o;
            try {
                o = con.newInstance();
            } catch (InvocationTargetException | ExceptionInInitializerError e) {
                Throwable cause = e.getCause();
                if (cause == null) cause = e;
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                throw new IllegalStateException(
                        "Unexpected error in constructor for TomlSerializable type (" + this.type.getName() + ")",
                        cause
                );
            } catch (ReflectiveOperationException | SecurityException e) {
                throw new IllegalStateException("Unexpected reflection error", e);
            }

            T qual = this.type.cast(o);
            return new Builder<>(this, qual);
        }

        @Override
        public @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull T instance) {
            Set<TomlKey> ret = new LinkedHashSet<>();
            this.keys0(ret, this.type);
            return Collections.unmodifiableSet(ret);
        }

        private void keys0(@NotNull Set<TomlKey> set, @NotNull Class<?> cls) {
            for (Field f : cls.getDeclaredFields())
                set.add(TomlKey.literal(f.getName()));
            cls = cls.getSuperclass();
            if (cls == null || !TomlSerializable.class.isAssignableFrom(cls)) return;
            this.keys0(set, cls);
        }

        private @NotNull Field resolveField(@NotNull TomlKey key) {
            if (key.size() != 1)
                throw new IllegalArgumentException("Illegal key size (expected 1, got " + key.size() + ")");

            String k0 = key.get(0);
            Class<?> cls = this.type;

            do {
                for (Field f : cls.getDeclaredFields()) {
                    if (k0.equals(f.getName())) return f;
                }
            } while ((cls = cls.getSuperclass()) != null);

            throw new IllegalArgumentException(
                    "Key \"" + k0 + "\" does not match any fields on TomlSerializable type " + this.type.getName()
            );
        }

        @Override
        public @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key) {
            return ParameterizedClass.of(this.resolveField(key));
        }

        @Override
        public @UnknownNullability Object get(@NotNull T instance, @NotNull TomlKey key) {
            Field f = this.resolveField(key);

            Throwable suppressed = null;
            try {
                f.setAccessible(true);
            } catch (Exception e) {
                suppressed = e;
            }

            try {
                return f.get(instance);
            } catch (IllegalAccessException e) {
                IllegalStateException ex = new IllegalStateException("Failed to access field \"" + f.getName() +
                        "\" on TomlSerializable type " + this.type.getName());
                if (suppressed != null) ex.addSuppressed(suppressed);
                throw ex;
            }
        }

        private static final class Builder<O extends TomlSerializable> implements TableTypeModel.Builder<O> {

            private final Serializable<O> parent;
            private final O instance;

            private Builder(@NotNull Serializable<O> parent, @NotNull O instance) {
                this.parent = parent;
                this.instance = instance;
            }

            @Override
            public void set(@NotNull TomlKey key, @NotNull Object value) {
                Field f = this.parent.resolveField(key);

                Throwable suppressed = null;
                try {
                    f.setAccessible(true);
                } catch (Exception e) {
                    suppressed = e;
                }

                try {
                    f.set(this.instance, value);
                } catch (IllegalAccessException e) {
                    IllegalStateException ex = new IllegalStateException("Failed to access field \"" + f.getName() +
                            "\" on TomlSerializable type " + this.parent.type.getName());
                    if (suppressed != null) ex.addSuppressed(suppressed);
                    throw ex;
                }
            }

            @Override
            public @NotNull O build() {
                return this.instance;
            }

        }

    }

    final class Record<T> implements TableTypeModel<T> {

        private final Class<T> clazz;
        private final RecordSupport.Component[] components;

        private Record(@NotNull Class<T> clazz) {
            this.clazz = clazz;
            this.components = RecordSupport.getRecordComponents(clazz);
        }

        //

        @Override
        public @NotNull Class<T> type() {
            return this.clazz;
        }

        @Override
        public @NotNull TableTypeModel.Builder<T> create() {
            return new Builder<>(this);
        }

        @Override
        public @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull T instance) {
            List<TomlKey> ret = new ArrayList<>(this.components.length);
            for (RecordSupport.Component c : this.components) {
                ret.add(TomlKey.literal(c.name()));
            }
            return Collections.unmodifiableList(ret);
        }

        private @NotNull RecordSupport.Component lookupComponent(@NotNull TomlKey key) {
            if (key.size() != 1)
                throw new IllegalArgumentException("Invalid key size (expected 1, got " + key.size() + ")");

            String k0 = key.get(0);

            for (RecordSupport.Component rc : this.components) {
                if (k0.equals(rc.name())) {
                    return rc;
                }
            }

            throw new IllegalArgumentException("Record " + this.clazz.getName() + " has no component named " + k0);
        }

        @Override
        public @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key) {
            return this.lookupComponent(key).type();
        }

        @Override
        public @UnknownNullability Object get(@NotNull T instance, @NotNull TomlKey key) {
            return this.lookupComponent(key).access(instance);
        }

        private static final class Builder<O> implements TableTypeModel.Builder<O> {

            private final Record<O> parent;
            private final Object[] values;

            private Builder(@NotNull Record<O> parent) {
                this.parent = parent;
                this.values = new Object[parent.components.length];
            }

            //

            @Override
            public void set(@NotNull TomlKey key, @NotNull Object value) {
                RecordSupport.Component component = this.parent.lookupComponent(key);
                this.values[component.index()] = value;
            }

            @Override
            public @NotNull O build() {
                return RecordSupport.createRecord(this.parent.clazz, this.values);
            }

        }

    }

    final class Toml implements TableTypeModel<TomlTable> {

        private static final Toml INSTANCE = new Toml();

        @Override
        public @NotNull Class<TomlTable> type() {
            return TomlTable.class;
        }

        @Override
        public @NotNull TableTypeModel.Builder<TomlTable> create() {
            return new Builder();
        }

        @Override
        public @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull TomlTable instance) {
            return instance.keys(false);
        }

        @Override
        public @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key) {
            return new ParameterizedClass<>(TomlValue.class);
        }

        @Override
        public @UnknownNullability Object get(@NotNull TomlTable instance, @NotNull TomlKey key) {
            return instance.get(key);
        }

        private static final class Builder implements TableTypeModel.Builder<TomlTable> {

            private final TomlTable table = TomlTable.create();

            @Override
            public void set(@NotNull TomlKey key, @NotNull Object value) {
                this.table.put(key, (TomlValue) value);
            }

            @Override
            public @NotNull TomlTable build() {
                return this.table;
            }

        }

    }

    final class StringMap<T extends Map<String, V>, V> implements TableTypeModel<T> {

        @SuppressWarnings("unchecked")
        private static <IT extends Map<String, IV>, IV> StringMap<?, ?> create(
                @NotNull Class<IT> mapClass,
                @NotNull ParameterizedClass<?> valueType
        ) {
            return new StringMap<>(mapClass, (ParameterizedClass<IV>) valueType);
        }

        //

        private final Class<T> clazz;
        private final ParameterizedClass<V> valueType;

        private StringMap(@NotNull Class<T> clazz, @NotNull ParameterizedClass<V> valueType) {
            this.clazz = clazz;
            this.valueType = valueType;
        }

        //

        @Override
        public @NotNull Class<T> type() {
            return this.clazz;
        }

        @Override
        public @NotNull TableTypeModel.Builder<T> create() {
            T map;
            if (this.clazz.isAssignableFrom(HashMap.class)) {
                map = this.clazz.cast(new HashMap<String, V>());
            } else {
                Object o;
                try {
                    o = this.clazz.getConstructor()
                            .newInstance();
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Map class " + this.clazz.getName() + " has no primary constructor");
                } catch (ReflectiveOperationException | SecurityException e) {
                    throw new IllegalStateException("Unexpected reflection error", e);
                }
                map = this.clazz.cast(o);
            }
            return new Builder<>(this, map);
        }

        @Override
        public @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull T instance) {
            Set<String> keys = instance.keySet();
            List<TomlKey> ret = new ArrayList<>(keys.size());
            for (String key : keys) ret.add(TomlKey.literal(key));
            return Collections.unmodifiableList(ret);
        }

        @Override
        public @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key) {
            return this.valueType;
        }

        @Override
        public @UnknownNullability Object get(@NotNull T instance, @NotNull TomlKey key) {
            if (key.size() != 1)
                throw new IllegalArgumentException("Illegal key size (expected 1, got " + key.size() + ")");

            return instance.get(key.get(0));
        }

        private static final class Builder<T extends Map<String, V>, V> implements TableTypeModel.Builder<T> {

            private final StringMap<T, V> parent;
            private final T map;

            private Builder(@NotNull StringMap<T, V> parent, @NotNull T map) {
                this.parent = parent;
                this.map = map;
            }

            //

            @Override
            public void set(@NotNull TomlKey key, @NotNull Object value) {
                if (key.size() != 1)
                    throw new IllegalArgumentException("Illegal key size (expected 1, got " + key.size() + ")");

                this.map.put(key.get(0), this.parent.valueType.raw().cast(value));
            }

            @Override
            public @NotNull T build() {
                return this.map;
            }

        }

    }

}
