package io.github.wasabithumb.jtoml.serial.reflect;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.util.ReferenceHolder;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;

public final class ReflectTomlSerializer<T extends TomlSerializable> implements TomlSerializer.Symmetric<T> {

    private final Class<T> clazz;
    private ReferenceHolder recursionGuard = null;

    public ReflectTomlSerializer(@NotNull Class<T> clazz) {
        this.clazz = clazz;
    }

    //

    @Override
    public @NotNull Class<T> serialType() {
        return this.clazz;
    }

    @Override
    public @NotNull T serialize(@NotNull TomlTable table) {
        // Locate constructor
        Constructor<T> con;
        try {
            con = this.clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot deserialize to " + this.clazz.getName() + " (no primary constructor)");
        }
        try {
            con.setAccessible(true);
        } catch (SecurityException ignored) { }

        // Invoke constructor
        T instance;
        try {
            instance = con.newInstance();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IllegalStateException(
                    "Primary constructor for class " + this.clazz.getName() + " raised a checked exception",
                    cause
            );
        } catch (ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IllegalStateException(
                    "Initializer for class " + this.clazz.getName() + " raised a checked exception",
                    cause
            );
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new AssertionError(
                    "Unexpected error while creating instance of " + this.clazz.getName(),
                    e
            );
        }

        this.forEachField(
                this.clazz,
                (Field f) -> this.writeFieldFromTable(instance, f, table)
        );
        return instance;
    }

    @Override
    public @NotNull TomlTable deserialize(@NotNull T data) {
        if (this.recursionGuard == null) this.recursionGuard = new ReferenceHolder();
        this.recursionGuard.add(data);
        TomlTable table = TomlTable.create();
        this.forEachField(
                data.getClass(),
                (Field f) -> this.readFieldToTable(data, f, table)
        );
        this.recursionGuard = null;
        return table;
    }

    //

    private void writeFieldFromTable(
            @NotNull T instance,
            @NotNull Field f,
            @NotNull TomlTable table
    ) {
        TomlValue value = table.get(TomlKey.literal(f.getName()));
        if (value == null) {
            throw new IllegalStateException("Cannot serialize to field \"" + f.getName() +
                    "\" (no matching value in table)");
        }
        Object object = this.convertValue(f.getType(), f.getGenericType(), value, Modifier.isFinal(f.getModifiers()));
        try {
            f.set(instance, object);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to serialize to field \"" + f.getName() + "\"", e);
        }
    }

    private @NotNull Object convertValue(
            @NotNull Class<?> ft,
            @NotNull Type fgt,
            @NotNull TomlValue value,
            boolean finalize
    ) {
        if (TomlSerializable.class.isAssignableFrom(ft)) {
            if (!value.isTable()) throw new IllegalStateException("Cannot unpack non-table value (" + value + ")");
            return this.anonymousSerialize(ft.asSubclass(TomlSerializable.class), value.asTable());
        } else if (Boolean.TYPE.equals(ft) || Boolean.class.equals(ft)) {
            return value.asPrimitive().asBoolean();
        } else if (Byte.TYPE.equals(ft) || Byte.class.equals(ft)) {
            return (byte) value.asPrimitive().asLong();
        } else if (Short.TYPE.equals(ft) || Short.class.equals(ft)) {
            return (short) value.asPrimitive().asLong();
        } else if (Integer.TYPE.equals(ft) || Integer.class.equals(ft)) {
            return value.asPrimitive().asInteger();
        } else if (Long.TYPE.equals(ft) || Long.class.equals(ft)) {
            return value.asPrimitive().asLong();
        } else if (Float.TYPE.equals(ft) || Float.class.equals(ft)) {
            return value.asPrimitive().asFloat();
        } else if (Double.TYPE.equals(ft) || Double.class.equals(ft)) {
            return value.asPrimitive().asDouble();
        } else if (Character.TYPE.equals(ft) || Character.class.equals(ft)) {
            TomlPrimitive tp = value.asPrimitive();
            if (tp.isInteger()) {
                return (char) tp.asInteger();
            } else {
                String s = tp.asString();
                if (s.length() != 1) throw new IllegalStateException("Cannot convert multi-char string to char");
                return s.charAt(0);
            }
        } else if (ft.isAssignableFrom(String.class)) {
            return value.asPrimitive().asString();
        } else if (ft.isAssignableFrom(OffsetDateTime.class)) {
            return value.asPrimitive().asOffsetDateTime();
        } else if (ft.isAssignableFrom(LocalDateTime.class)) {
            return value.asPrimitive().asLocalDateTime();
        } else if (ft.isAssignableFrom(LocalDate.class)) {
            return value.asPrimitive().asLocalDate();
        } else if (ft.isAssignableFrom(LocalTime.class)) {
            return value.asPrimitive().asLocalTime();
        } else if (ft.isAssignableFrom(Map.class)) {
            Map<String, Object> map = new HashMap<>();
            if (!value.isTable()) throw new IllegalStateException("Cannot unpack non-table value (" + value + ")");
            Type valueType = ((ParameterizedType) fgt).getActualTypeArguments()[1];

            TomlTable t = value.asTable();
            TomlValue tv;
            for (TomlKey key : t.keys(false)) {
                tv = t.get(key);
                assert tv != null;
                map.put(key.toString(), this.convertValue((Class<?>) valueType, valueType, tv, finalize));
            }

            if (finalize) map = Collections.unmodifiableMap(map);
            return map;
        } else if (ft.isAssignableFrom(List.class)) {
            if (!value.isArray()) throw new IllegalStateException("Cannot read non-array value as List (" + value + ")");
            Type valueType = ((ParameterizedType) fgt).getActualTypeArguments()[0];

            TomlArray a = value.asArray();
            List<Object> list = new ArrayList<>(a.size());

            for (TomlValue tv : a) {
                list.add(this.convertValue((Class<?>) valueType, valueType, tv, finalize));
            }

            if (finalize) list = Collections.unmodifiableList(list);
            return list;
        } else if (Object.class.equals(ft)) {
            return value.asPrimitive().value();
        } else {
            throw new IllegalStateException("No rule to convert TOML value to type " + ft.getName());
        }
    }

    private void readFieldToTable(
            @NotNull T instance,
            @NotNull Field f,
            @NotNull TomlTable table
    ) {
        Object fv;
        try {
            fv = f.get(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot deserialize from field \"" + f.getName() + "\"", e);
        }
        if (fv == null) {
            throw new IllegalStateException("Cannot convert value of field \"" + f.getName() + "\" to TOML (is null)");
        }
        table.put(TomlKey.literal(f.getName()), this.convertObject(fv));
    }

    private @NotNull TomlValue convertObject(@NotNull Object o) {
        if (!this.recursionGuard.add(o))
            throw new IllegalStateException("Recursion detected in de/serialization (object: " + o + ")");
        final Class<?> ot = o.getClass();

        if (o instanceof TomlSerializable) {
            return this.anonymousDeserialize(
                    ot.asSubclass(TomlSerializable.class),
                    o
            );
        } else if (o instanceof CharSequence) {
            return TomlPrimitive.of(o.toString());
        } else if (o instanceof Number) {
            if (o instanceof Double || o instanceof Float) {
                return TomlPrimitive.of(((Number) o).doubleValue());
            } else {
                return TomlPrimitive.of(((Number) o).longValue());
            }
        } else if (o instanceof Character) {
            return TomlPrimitive.of(Character.toString((Character) o));
        } else if (o instanceof OffsetDateTime) {
            return TomlPrimitive.of((OffsetDateTime) o);
        } else if (o instanceof LocalDateTime) {
            return TomlPrimitive.of((LocalDateTime) o);
        } else if (o instanceof LocalDate) {
            return TomlPrimitive.of((LocalDate) o);
        } else if (o instanceof LocalTime) {
            return TomlPrimitive.of((LocalTime) o);
        } else if (o instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) o;
            TomlTable ret = TomlTable.create();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = Objects.toString(entry.getKey());
                Object value = entry.getValue();
                if (value == null) continue;
                ret.put(TomlKey.literal(key), this.convertObject(value));
            }

            return ret;
        } else if (o instanceof Collection) {
            Collection<?> c = (Collection<?>) o;
            TomlArray ret = TomlArray.create(c.size());

            for (Object value : c) {
                if (value == null) continue;
                ret.add(this.convertObject(value));
            }

            return ret;
        } else {
            throw new IllegalStateException("No rule to convert object of type " + ot.getName());
        }
    }

    private void forEachField(@NotNull Class<?> clazz, @NotNull Consumer<Field> fn) {
        int mod;
        for (Field f : clazz.getDeclaredFields()) {
            mod = f.getModifiers();
            if (Modifier.isStatic(mod)) continue;
            if (Modifier.isTransient(mod)) continue;
            try {
                f.setAccessible(true);
            } catch (Exception ignored) { }
            fn.accept(f);
        }
        Class<?> sup = clazz.getSuperclass();
        if (sup != null) this.forEachField(sup, fn);
    }

    private <Q extends TomlSerializable> @NotNull Q anonymousSerialize(
            @NotNull Class<Q> clazz,
            @NotNull TomlTable table
    ) {
        ReflectTomlSerializer<Q> child = new ReflectTomlSerializer<>(clazz);
        return child.serialize(table);
    }

    private <Q extends TomlSerializable> @NotNull TomlTable anonymousDeserialize(
            @NotNull Class<Q> clazz,
            @NotNull Object data
    ) {
        ReflectTomlSerializer<Q> child = new ReflectTomlSerializer<>(clazz);
        child.recursionGuard = this.recursionGuard;
        return child.deserialize(clazz.cast(data));
    }

}
