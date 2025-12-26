package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.Key;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

@ApiStatus.Internal
final class SerializableTableTypeModel<T extends TomlSerializable> extends AbstractTableTypeModel<T> {

    @Contract("_ -> new")
    static @NotNull SerializableTableTypeModel<?> create(@NotNull Class<?> cls) {
        return new SerializableTableTypeModel<>(cls.asSubclass(TomlSerializable.class));
    }

    private static @NotNull Map<TomlKey, Field> buildFieldMap(@NotNull Class<?> cls) {
        Map<TomlKey, Field> ret = new HashMap<>();
        buildFieldMap0(cls, ret);
        return Collections.unmodifiableMap(ret);
    }

    private static void buildFieldMap0(@NotNull Class<?> cls, @NotNull Map<TomlKey, Field> map) {
        for (Field f : cls.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;
            buildFieldMap00(f, map);
        }
        cls = cls.getSuperclass();
        if (cls == null || !TomlSerializable.class.isAssignableFrom(cls)) return;
        buildFieldMap0(cls, map);
    }

    private static void buildFieldMap00(@NotNull Field f, @NotNull Map<TomlKey, Field> map) {
        String name = f.getName();
        Key annotation = f.getAnnotation(Key.class);
        if (annotation != null) name = annotation.value();
        TomlKey key = TomlKey.literal(name);
        Field existing = map.put(key, f);
        if (existing != null) {
            throw new IllegalStateException("Serializable field (" + f.getName() + ") with key " + key +
                    " shadows field (" + existing.getName() + ") with same key declared in class " +
                    existing.getDeclaringClass().getName());
        }
    }

    //

    private final Class<T> type;
    private final Map<TomlKey, Field> fieldMap;

    private SerializableTableTypeModel(@NotNull Class<T> type) {
        this.type = type;
        this.fieldMap = buildFieldMap(type);
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
        return this.fieldMap.keySet();
    }

    private @NotNull Field resolveField(@NotNull TomlKey key) {
        if (key.size() != 1)
            throw new IllegalArgumentException("Illegal key size (expected 1, got " + key.size() + ")");

        Field ret = this.fieldMap.get(key);
        if (ret != null) return ret;

        throw new IllegalArgumentException(
                "Key \"" + key.get(0) + "\" does not match any fields on TomlSerializable type " + this.type.getName()
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

    @Override
    public void applyTableComments(@NotNull Comments comments) {
        applyAnnotationComments(this.type.getDeclaredAnnotations(), comments);
    }

    @Override
    public void applyFieldComments(@NotNull TomlKey key, @NotNull Comments comments) {
        Field f = this.resolveField(key);
        applyAnnotationComments(f.getDeclaredAnnotations(), comments);
    }

    //

    private static final class Builder<O extends TomlSerializable> implements TableTypeModel.Builder<O> {

        private final SerializableTableTypeModel<O> parent;
        private final O instance;

        private Builder(@NotNull SerializableTableTypeModel<O> parent, @NotNull O instance) {
            this.parent = parent;
            this.instance = instance;
        }

        private void trySetModifiers(@NotNull Field field, int modifiers) {
            try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, modifiers);
            } catch (ReflectiveOperationException | SecurityException ignored) { }
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

            final int modifiers = f.getModifiers();
            boolean isFinal = Modifier.isFinal(modifiers);
            if (isFinal)
                this.trySetModifiers(f, modifiers & ~Modifier.FINAL);

            try {
                f.set(this.instance, value);
            } catch (IllegalAccessException e) {
                IllegalStateException ex = new IllegalStateException("Failed to access field \"" + f.getName() +
                        "\" on TomlSerializable type " + this.parent.type.getName());
                if (suppressed != null) ex.addSuppressed(suppressed);
                throw ex;
            } finally {
                if (isFinal)
                    this.trySetModifiers(f, modifiers);
            }
        }

        @Override
        public @NotNull O build() {
            return this.instance;
        }

    }

}
