package io.github.wasabithumb.jtoml.serial.reflect.model.array;

import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.reflect.Array;

@ApiStatus.Internal
final class DirectArrayTypeModel<T> implements ArrayTypeModel<T> {

    private final Class<T> arrayType;
    private final Class<?> elementType;

    DirectArrayTypeModel(@NotNull Class<T> arrayType, @NotNull Class<?> elementType) {
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