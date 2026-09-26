/*
 * Copyright 2026 Xavier Pedraza
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

package io.github.wasabithumb.jtoml.value.array;

import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * A list of {@link TomlValue TOML values}.
 * @see #create(int)
 * @see #create()
 */
@ApiStatus.NonExtendable
public interface TomlArray extends List<TomlValue>, RandomAccess, TomlValue {

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
    @ApiStatus.AvailableSince("0.6.3")
    static @NotNull TomlArray copyOf(@NotNull Iterable<? extends TomlValue> array) {
        return TomlArrayImpl.copyOf(array);
    }

    //

    /**
     * Reports the number of elements in this array.
     */
    @Override
    @Contract(pure = true)
    int size();

    /**
     * Returns the Nth element in this array.
     * @throws IndexOutOfBoundsException Index is less than 0 or not less than {@link #size()}
     */
    @Override
    @Contract(pure = true)
    @NotNull TomlValue get(int index) throws IndexOutOfBoundsException;

    /**
     * Adds a new element to this array.
     * @throws NullPointerException Value is null
     * @throws IndexOutOfBoundsException Index is less than 0 or not less than or equal to {@link #size()}
     */
    @Override
    @Contract(value = "_, null -> fail", mutates = "this")
    void add(int index, TomlValue element);

    /**
     * Adds a new element to this array.
     * @throws NullPointerException Value is null
     */
    @Override
    @Contract(value = "null -> fail; !null -> true", mutates = "this")
    boolean add(TomlValue value);

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @throws NullPointerException Value is null
     * @see TomlPrimitive#of(String)
     */
    @Contract(mutates = "this")
    default void add(@NotNull String value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @see TomlPrimitive#of(boolean)
     */
    @Contract(mutates = "this")
    default void add(boolean value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @see TomlPrimitive#of(long)
     */
    @Contract(mutates = "this")
    default void add(long value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @see TomlPrimitive#of(int)
     */
    @Contract(mutates = "this")
    default void add(int value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @see TomlPrimitive#of(double)
     */
    @Contract(mutates = "this")
    default void add(double value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @see TomlPrimitive#of(float)
     */
    @Contract(mutates = "this")
    default void add(float value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @see TomlPrimitive#of(OffsetDateTime)
     * @throws NullPointerException Value is null
     */
    @Contract(mutates = "this")
    default void add(@NotNull OffsetDateTime value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @see TomlPrimitive#of(LocalDateTime)
     * @throws NullPointerException Value is null
     */
    @Contract(mutates = "this")
    default void add(@NotNull LocalDateTime value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @see TomlPrimitive#of(LocalDate)
     * @throws NullPointerException Value is null
     */
    @Contract(mutates = "this")
    default void add(@NotNull LocalDate value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds a new element to this array after wrapping it into a {@link TomlPrimitive}.
     * @see TomlPrimitive#of(LocalTime)
     * @throws NullPointerException Value is null
     */
    @Contract(mutates = "this")
    default void add(@NotNull LocalTime value) {
        this.add(TomlPrimitive.of(value));
    }

    /**
     * Adds to this array all the values
     * contained within {@code source}.
     * @deprecated Use {@link #addAll(Collection)}.
     */
    @Deprecated
    default void addAll(@NotNull Iterable<? extends TomlValue> source) {
        for (TomlValue tv : source) this.add(tv);
    }

    /**
     * Returns true if the given value is present within the array.
     */
    @Override
    @Contract("null -> false")
    boolean contains(Object value);

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
    @Override
    @Contract(value = "null -> false", mutates = "this")
    boolean remove(Object value);

    /**
     * Sets the Nth element of this array to the given value
     * @return The value previously set at this index
     * @throws IndexOutOfBoundsException Index is less than 0 or not less than {@link #size()}
     * @throws NullPointerException Value is null
     */
    @Contract(value = "_, null -> fail", mutates = "this")
    @NotNull TomlValue set(int index, TomlValue value) throws IndexOutOfBoundsException;

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
