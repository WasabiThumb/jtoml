package io.github.wasabithumb.jtoml.value.array;

import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.RandomAccess;

/**
 * A list of {@link TomlValue TOML values}
 * @see #create(int)
 * @see #create()
 */
@ApiStatus.NonExtendable
public interface TomlArray extends Iterable<TomlValue>, RandomAccess, TomlValue {

    /** Creates a new TomlArray with the specified initial capacity */
    @Contract("_ -> new")
    static @NotNull TomlArray create(int initialCapacity) {
        return new TomlArrayImpl(initialCapacity);
    }

    /** Creates a new empty TomlArray */
    @Contract("-> new")
    static @NotNull TomlArray create() {
        return new TomlArrayImpl();
    }

    /**
     * Creates a new mutable TomlArray with the same
     * content as the provided array
     */
    @Contract("_ -> new")
    static @NotNull TomlArray copyOf(@NotNull Iterable<? extends TomlValue> array) {
        if (array instanceof TomlArray) {
            return TomlArrayImpl.copyOf((TomlArrayImpl) array);
        } else {
            TomlArray ret;
            if (array instanceof Collection<?>) {
                ret = create(((Collection<?>) array).size());
            } else {
                ret = create();
            }
            ret.addAll(array);
            return ret;
        }
    }

    //

    /**
     * Reports the number of elements in this array
     */
    @Contract(pure = true)
    int size();

    /**
     * Returns the Nth element in this array
     * @throws IndexOutOfBoundsException Index is less than 0 or not less than {@link #size()}
     */
    @NotNull TomlValue get(int index) throws IndexOutOfBoundsException;

    /**
     * Adds a new element to this array
     * @throws NullPointerException Value is null
     */
    @Contract(value = "null -> fail", mutates = "this")
    void add(TomlValue value);

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}
     * @throws NullPointerException Value is null
     * @see TomlPrimitive#of(String)
     */
    @Contract(mutates = "this")
    default void add(@NotNull String value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}
     * @see TomlPrimitive#of(boolean)
     */
    @Contract(mutates = "this")
    default void add(boolean value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}
     * @see TomlPrimitive#of(long)
     */
    @Contract(mutates = "this")
    default void add(long value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}
     * @see TomlPrimitive#of(int)
     */
    @Contract(mutates = "this")
    default void add(int value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}
     * @see TomlPrimitive#of(double)
     */
    @Contract(mutates = "this")
    default void add(double value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}
     * @see TomlPrimitive#of(float)
     */
    @Contract(mutates = "this")
    default void add(float value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds to this array all the values
     * contained within {@code source}
     */
    default void addAll(@NotNull Iterable<? extends TomlValue> source) {
        for (TomlValue tv : source) this.add(tv);
    }

    /**
     * Returns true if the given value is present within the array
     */
    @Contract("null -> false")
    default boolean contains(TomlValue value) {
        if (value == null) return false;
        for (int i=0; i < this.size(); i++) {
            if (this.get(i).equals(value)) return true;
        }
        return false;
    }

    /**
     * Removes the Nth element from this array
     * @return The element that was removed
     * @throws IndexOutOfBoundsException Index is less than 0 or not less than {@link #size()}
     */
    @Contract(value = "_ -> !null", mutates = "this")
    TomlValue remove(int index) throws IndexOutOfBoundsException;

    /**
     * Removes the first occurrence of the specified value from this array
     * @return True if any element was removed
     */
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

    /**
     * Sets the Nth element of this array to the given value
     * @return The value previously set at this index
     * @throws IndexOutOfBoundsException Index is less than 0 or not less than {@link #size()}
     */
    @Contract(value = "_, null -> fail", mutates = "this")
    @NotNull TomlValue set(int index, TomlValue value) throws IndexOutOfBoundsException;

    /**
     * Returns a new array with the same length and values as this object
     */
    @Contract("-> new")
    default @NotNull TomlValue @NotNull [] toArray() {
        final int len = this.size();
        TomlValue[] vs = new TomlValue[len];
        for (int i=0; i < len; i++) vs[i] = this.get(i);
        return vs;
    }

    /**
     * Returns a new array with the same length and values as this object,
     * asserting that each element is an instance of the given class
     * @throws ClassCastException One or more elements could not be cast to the given type
     */
    @SuppressWarnings("unchecked")
    @Contract("_ -> new")
    default <T extends TomlValue> @NotNull T @NotNull [] toArray(@NotNull Class<T> valueType) throws ClassCastException {
        final int len = this.size();
        T[] ret = (T[]) Array.newInstance(valueType, len);
        for (int i=0; i < len; i++) ret[i] = valueType.cast(this.get(i));
        return ret;
    }

}
