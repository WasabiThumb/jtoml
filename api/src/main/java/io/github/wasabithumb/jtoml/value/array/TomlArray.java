package io.github.wasabithumb.jtoml.value.array;

import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.RandomAccess;

@ApiStatus.NonExtendable
public interface TomlArray extends Iterable<TomlValue>, RandomAccess, TomlValue {

    @Contract("_ -> new")
    static @NotNull TomlArray create(int initialCapacity) {
        return new TomlArrayImpl(initialCapacity);
    }

    @Contract("-> new")
    static @NotNull TomlArray create() {
        return new TomlArrayImpl();
    }

    //

    @Contract(pure = true)
    int size();

    @NotNull TomlValue get(int index) throws IndexOutOfBoundsException;

    @Contract(value = "null -> fail", mutates = "this")
    void add(TomlValue value);

    @Contract(mutates = "this")
    default void add(@NotNull String value) {
        this.add(TomlPrimitive.of(value));
    }

    @Contract(mutates = "this")
    default void add(boolean value) {
        this.add(TomlPrimitive.of(value));
    }

    @Contract(mutates = "this")
    default void add(long value) {
        this.add(TomlPrimitive.of(value));
    }

    @Contract(mutates = "this")
    default void add(int value) {
        this.add(TomlPrimitive.of(value));
    }

    @Contract(mutates = "this")
    default void add(double value) {
        this.add(TomlPrimitive.of(value));
    }

    @Contract(mutates = "this")
    default void add(float value) {
        this.add(TomlPrimitive.of(value));
    }

    default void addAll(@NotNull Iterable<? extends TomlValue> source) {
        for (TomlValue tv : source) this.add(tv);
    }

    @Contract("null -> false")
    default boolean contains(TomlValue value) {
        if (value == null) return false;
        for (int i=0; i < this.size(); i++) {
            if (this.get(i).equals(value)) return true;
        }
        return false;
    }

    @Contract(value = "_ -> !null", mutates = "this")
    TomlValue remove(int index) throws IndexOutOfBoundsException;

    @Contract(value = "null -> false", mutates = "this")
    default boolean remove(TomlValue value) {
        if (value == null) return false;
        for (int i=0; i < this.size(); i++) {
            if (this.get(i).equals(value)) {
                this.remove(i);
                return true;
            }
        }
        return false;
    }

    @Contract(value = "_, null -> fail", mutates = "this")
    @Nullable TomlValue set(int index, TomlValue value) throws IndexOutOfBoundsException;

    @Contract("-> new")
    default @NotNull TomlValue @NotNull [] toArray() {
        final int len = this.size();
        TomlValue[] vs = new TomlValue[len];
        for (int i=0; i < len; i++) vs[i] = this.get(i);
        return vs;
    }

    @SuppressWarnings("unchecked")
    @Contract("_ -> new")
    default <T extends TomlValue> @NotNull T @NotNull [] toArray(@NotNull Class<T> valueType) throws ClassCastException {
        final int len = this.size();
        T[] ret = (T[]) Array.newInstance(valueType, len);
        for (int i=0; i < len; i++) ret[i] = valueType.cast(this.get(i));
        return ret;
    }

}
