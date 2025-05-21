package io.github.wasabithumb.jtoml.serial.reflect.model;

import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ApiStatus.Internal
public interface ArrayTypeModel<T> extends TypeModel<T> {

    @SuppressWarnings("unchecked")
    static <O> @Nullable ArrayTypeModel<O> match(@NotNull ParameterizedClass<O> pc) {
        Class<O> raw = pc.raw();

        // TomlArray
        if (TomlArray.class.equals(raw))
            return (ArrayTypeModel<O>) Toml.INSTANCE;

        // Array
        if (raw.isArray())
            return new Direct<>(raw, raw.getComponentType());

        // List<?>
        ParameterizedClass<?> list = pc.declaredInterface(List.class);
        if (list != null && list.paramCount() >= 1) {
            ParameterizedClass<?> elementType = ParameterizedClass.of(list.param(0));
            return (ArrayTypeModel<O>) OfList.create(raw.asSubclass(List.class), elementType);
        }

        // Other
        return null;
    }

    //

    @NotNull ParameterizedClass<?> componentType();

    @NotNull T createNew(int length);

    int size(@NotNull T instance);

    @UnknownNullability Object get(@NotNull T instance, int index);

    void set(@NotNull T instance, int index, @NotNull Object object);

    //

    final class Direct<T> implements ArrayTypeModel<T> {

        private final Class<T> arrayType;
        private final Class<?> elementType;

        private Direct(@NotNull Class<T> arrayType, @NotNull Class<?> elementType) {
            this.arrayType = arrayType;
            this.elementType = elementType;
        }

        //

        @Override
        public @NotNull Class<T> type() {
            return this.arrayType;
        }

        @Override
        public @NotNull ParameterizedClass<?> componentType() {
            return new ParameterizedClass<>(this.elementType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull T createNew(int length) {
            return (T) Array.newInstance(this.elementType, length);
        }

        @Override
        public int size(@NotNull T instance) {
            return Array.getLength(instance);
        }

        @Override
        public @UnknownNullability Object get(@NotNull T instance, int index) {
            return Array.get(instance, index);
        }

        @Override
        public void set(@NotNull T instance, int index, @NotNull Object object) {
            Array.set(instance, index, this.elementType.cast(object));
        }

    }

    final class Toml implements ArrayTypeModel<TomlArray> {

        private static final Toml INSTANCE = new Toml();

        @Override
        public @NotNull Class<TomlArray> type() {
            return TomlArray.class;
        }

        @Override
        public @NotNull ParameterizedClass<?> componentType() {
            return new ParameterizedClass<>(TomlValue.class);
        }

        @Override
        public @NotNull TomlArray createNew(int length) {
            return TomlArray.create(length);
        }

        @Override
        public int size(@NotNull TomlArray instance) {
            return instance.size();
        }

        @Override
        public @UnknownNullability Object get(@NotNull TomlArray instance, int index) {
            return instance.get(index);
        }

        @Override
        public void set(@NotNull TomlArray instance, int index, @NotNull Object object) {
            if (index == instance.size()) {
                instance.add((TomlValue) object);
            } else {
                instance.set(index, (TomlValue) object);
            }
        }

    }

    final class OfList<T extends List<E>, E> implements ArrayTypeModel<T> {

        @SuppressWarnings("unchecked")
        private static <IT extends List<IE>, IE> OfList<IT, IE> create(
                @NotNull Class<IT> listType,
                @NotNull ParameterizedClass<?> elementType
        ) {
            return new OfList<>(listType, (ParameterizedClass<IE>) elementType);
        }

        //

        private final Class<T> type;
        private final ParameterizedClass<E> elementType;

        private OfList(@NotNull Class<T> type, @NotNull ParameterizedClass<E> elementType) {
            this.type = type;
            this.elementType = elementType;
        }

        //

        @Override
        public @NotNull Class<T> type() {
            return this.type;
        }

        @Override
        public @NotNull ParameterizedClass<?> componentType() {
            return this.elementType;
        }

        @Override
        public @NotNull T createNew(int length) {
            if (this.type.isAssignableFrom(ArrayList.class))
                return this.type.cast(new ArrayList<E>(length));

            if (this.type.isAssignableFrom(LinkedList.class))
                return this.type.cast(new LinkedList<E>());

            Constructor<?> con;
            boolean withLength;

            try {
                con = this.type.getConstructor(Integer.TYPE);
                withLength = true;
            } catch (NoSuchMethodException e1) {
                try {
                    con = this.type.getConstructor();
                    withLength = false;
                } catch (NoSuchMethodException e2) {
                    IllegalStateException e = new IllegalStateException("No suitable constructor for list type " +
                            this.type.getName());
                    e.addSuppressed(e1);
                    e.addSuppressed(e2);
                    throw e;
                }
            }

            Object ret;
            try {
                ret = withLength ? con.newInstance(length) :
                        con.newInstance();
            } catch (InvocationTargetException | ExceptionInInitializerError e) {
                Throwable cause = e.getCause();
                if (cause == null) cause = e;
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                throw new IllegalStateException("Unexpected error in list constructor", cause);
            } catch (ReflectiveOperationException | SecurityException e) {
                throw new IllegalStateException("Unexpected reflection error", e);
            }

            return this.type.cast(ret);
        }

        @Override
        public int size(@NotNull T instance) {
            return instance.size();
        }

        @Override
        public @UnknownNullability Object get(@NotNull T instance, int index) {
            return instance.get(index);
        }

        @Override
        public void set(@NotNull T instance, int index, @NotNull Object object) {
            if (index == instance.size()) {
                instance.add(this.elementType.raw().cast(object));
            } else {
                instance.set(index, this.elementType.raw().cast(object));
            }
        }

    }

}
