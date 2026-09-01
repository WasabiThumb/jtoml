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
import io.github.wasabithumb.jtoml.serial.reflect.model.TypeModelOptions;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.*;

import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @apiNote Formerly {@code SerializableTableTypeModel}
 */
@ApiStatus.Internal
final class PojoTableTypeModel<T> extends AbstractTableTypeModel<T> {

    private static @NotNull Spliterator<Class<?>> hierarchy(
            @NotNull Class<?> base,
            @NotNull TypeModelOptions options
    ) {
        final Iterator<Class<?>> src = new Iterator<Class<?>>() {
            private Class<?> next = base;

            @Override
            public boolean hasNext() {
                Class<?> next = this.next;
                if (next == null) return false;
                return options.ignoreMarker() ?
                        !Object.class.equals(next) :
                        TomlSerializable.class.isAssignableFrom(next);
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

    private static @NotNull Stream<Field> fieldStream(
            @NotNull Class<?> clazz,
            @NotNull TypeModelOptions options
    ) {
        return StreamSupport.stream(hierarchy(clazz, options), false)
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

    private static boolean hasNoArgsConstructor(@NotNull Class<?> cls) {
        for (Constructor<?> ctor : cls.getDeclaredConstructors()) {
            if (ctor.getParameterCount() == 0) return true;
        }
        return false;
    }

    //

    private final Class<T> type;
    private final TypeModelOptions options;

    PojoTableTypeModel(
            @NotNull Class<T> type,
            @NotNull TypeModelOptions options
    ) {
        this.type = type;
        this.options = options;
    }

    //

    @Override
    public @NotNull Class<T> type() {
        return this.type;
    }

    @Override
    public @NotNull TableTypeModel.Builder<T> create() {
        int modifiers = this.type.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))
            throw new IllegalStateException("Type " + this.type.getName() + " is not instantiable (interface or abstract class)");

        Instantiator instantiator = (!this.options.allowUnsafe() || hasNoArgsConstructor(this.type)) ?
                Instantiator.BASIC : Instantiator.UNSAFE;

        T instance = instantiator.instantiate(this.type);
        return new Builder<>(this, instance);
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
        return fieldStream(type, this.options)
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

    private static final class Builder<O> implements TableTypeModel.Builder<O> {

        private final PojoTableTypeModel<O> parent;
        private final O instance;

        private Builder(@NotNull PojoTableTypeModel<O> parent, @NotNull O instance) {
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

        //

        @Override
        protected Class<?> typeClassOf(Field member) {
            return member.getType();
        }

        @Override
        protected @Nullable Object nonSpecificDefault(Class<?> type) {
            return null;
        }

    }

    @FunctionalInterface
    private interface Instantiator {

        Instantiator BASIC = new Basic();
        Instantiator UNSAFE = Unsafe.tryCreate();


        //

        <T> @NotNull T instantiate(@NotNull Class<T> clazz);

        //

        final class Basic implements Instantiator {

            @Override
            public @NotNull <T> T instantiate(@NotNull Class<T> clazz) {
                Constructor<?> con;
                try {
                    con = clazz.getDeclaredConstructor();
                } catch (NoSuchMethodException e1) {
                    throw new IllegalStateException(
                            "No primary constructor for type (" + clazz.getName() +
                                    ") and unsafe instantiation is not enabled",
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
                            "Unexpected error in constructor for type (" + clazz.getName() + ")",
                            cause
                    );
                } catch (ReflectiveOperationException | SecurityException e) {
                    throw new IllegalStateException("Unexpected reflection error", e);
                }

                return clazz.cast(o);
            }

        }

        final class Unsafe implements Instantiator {

            static @NotNull Instantiator tryCreate() {
                // Adapted from Gson: https://github.com/google/gson/blob/004e7a4949e08b430e3c8996998ee5a17ff9423a/gson/src/main/java/com/google/gson/internal/UnsafeAllocator.java#L51

                try {
                    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                    Field f = unsafeClass.getDeclaredField("theUnsafe");
                    f.setAccessible(true);
                    Object unsafe = f.get(null);
                    Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
                    return new Unsafe(allocateInstance, unsafe, new Object[0]);
                } catch (Exception ignored) { }

                try {
                    Method getConstructorId = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", Class.class);
                    getConstructorId.setAccessible(true);
                    int constructorId = (Integer) getConstructorId.invoke(null, Object.class);
                    Method newInstance = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, int.class);
                    newInstance.setAccessible(true);
                    return new Unsafe(newInstance, null, new Object[] { constructorId });
                } catch (Exception ignored) { }

                try {
                    Method newInstance = ObjectInputStream.class.getDeclaredMethod("newInstance", Class.class, Class.class);
                    newInstance.setAccessible(true);
                    return new Unsafe(newInstance, null, new Object[] { Object.class });
                } catch (Exception ignored) { }

                return new Unsupported("Attempted to use unsafe allocation in an incompatible runtime environment");
            }

            //

            private final Method method;
            private final Object target;
            private final Object[] extraArgs;

            private Unsafe(
                    @NotNull Method method,
                    @Nullable Object target,
                    @UnknownNullability Object @NotNull [] extraArgs
            ) {
                this.method = method;
                this.target = target;
                this.extraArgs = extraArgs;
            }

            //

            @Override
            public @NotNull <T> T instantiate(@NotNull Class<T> clazz) {
                // Instantiate
                Object instance;
                Object[] args = new Object[this.extraArgs.length + 1];
                args[0] = clazz;
                System.arraycopy(this.extraArgs, 0, args, 1, this.extraArgs.length);
                try {
                    instance = this.method.invoke(this.target, args);
                } catch (Exception e) {
                    throw new IllegalStateException("Unsafe instantiation failed for class " + clazz.getName(), e);
                }
                return clazz.cast(instance);
            }

        }

        final class Unsupported implements Instantiator {

            private final String message;

            Unsupported(@NotNull String message) {
                this.message = message;
            }

            //

            @Override
            public @NotNull <T> T instantiate(@NotNull Class<T> clazz) {
                throw new UnsupportedOperationException(this.message);
            }

        }

    }

}
