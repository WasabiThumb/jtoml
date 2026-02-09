/*
 * Copyright 2025 Xavier Pedraza
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

package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.convention.KeyConvention;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApiStatus.Internal
final class SerializableTableTypeModel<T extends TomlSerializable> extends AbstractTableTypeModel<T> {

    @Contract("_ -> new")
    static @NotNull SerializableTableTypeModel<?> create(@NotNull Class<?> cls) {
        return new SerializableTableTypeModel<>(cls.asSubclass(TomlSerializable.class));
    }

    private static @NotNull Spliterator<Class<?>> hierarchy(final @NotNull Class<?> base) {
        final Iterator<Class<?>> src = new Iterator<Class<?>>() {
            private Class<?> next = base;

            @Override
            public boolean hasNext() {
                return this.next != null && TomlSerializable.class.isAssignableFrom(this.next);
            }

            @Override
            public Class<?> next() {
                Class<?> ret = this.next;
                this.next = ret.getSuperclass();
                return ret;
            }
        };
        return Spliterators.spliteratorUnknownSize(
                src,
                Spliterator.DISTINCT |
                        Spliterator.ORDERED |
                        Spliterator.NONNULL |
                        Spliterator.IMMUTABLE
        );
    }

    private static @NotNull Stream<Field> fieldStream(final @NotNull Class<?> clazz) {
        return StreamSupport.stream(hierarchy(clazz), false)
                .flatMap((Class<?> cls) -> Stream.of(cls.getDeclaredFields()))
                .filter((Field f) -> {
                    if (f.isSynthetic()) return false;
                    final int mod = f.getModifiers();
                    return !Modifier.isStatic(mod) && !Modifier.isTransient(mod);
                });
    }

    private static @NotNull Key fieldKey(@NotNull Field field, @NotNull KeyConvention defaultConvention) {
        return new FieldKey(field, defaultConvention);
    }

    private static @NotNull Field unwrapFieldKey(@NotNull Key key) {
        if (key instanceof FieldKey) {
            return ((FieldKey) key).member;
        }
        throw new IllegalArgumentException("Key " + key + " is not a FieldKey");
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
    public @NotNull Mapper mapper(@NotNull KeyConvention defaultConvention) {
        return new FixedMapper(this, this.keys(this.type, defaultConvention));
    }

    @Override
    public @NotNull @Unmodifiable Collection<Key> keys(@NotNull T instance, @NotNull KeyConvention defaultConvention) {
        return this.keys(instance.getClass(), defaultConvention);
    }

    private @NotNull @Unmodifiable Collection<Key> keys(@NotNull Class<?> type, @NotNull KeyConvention defaultConvention) {
        return fieldStream(type)
                .map((Field f) -> fieldKey(f, defaultConvention))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull ParameterizedClass<?> elementType(@NotNull Key key) {
        return ParameterizedClass.of(unwrapFieldKey(key));
    }

    @Override
    public @UnknownNullability Object get(@NotNull T instance, @NotNull Key key) {
        Field f = unwrapFieldKey(key);

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
        applyAnnotationComments(this.type, comments);
    }

    @Override
    public void applyFieldComments(@NotNull Key key, @NotNull Comments comments) {
        Field f = unwrapFieldKey(key);
        applyAnnotationComments(f, comments);
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
        public void set(@NotNull Key key, @NotNull Object value) {
            Field f = unwrapFieldKey(key);

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

    private static final class FieldKey extends MemberKey<Field> {

        FieldKey(
                @NotNull Field field,
                @NotNull KeyConvention defaultConvention
        ) {
            super(field, defaultConvention);
        }

    }

}
