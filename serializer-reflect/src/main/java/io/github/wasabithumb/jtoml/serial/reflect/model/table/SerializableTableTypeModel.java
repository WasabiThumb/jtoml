package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@ApiStatus.Internal
final class SerializableTableTypeModel<T extends TomlSerializable> extends AbstractTableTypeModel<T> {

    @Contract("_ -> new")
    static @NotNull SerializableTableTypeModel<?> create(@NotNull Class<?> cls) {
        return new SerializableTableTypeModel<>(cls.asSubclass(TomlSerializable.class));
    }

    //

    private final Class<T> type;

    private SerializableTableTypeModel(@NotNull Class<T> type) {
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
