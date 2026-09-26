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

package io.github.wasabithumb.jtoml.value.table;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TOML key-values, essentially a
 * {@code Map<TomlKey, TomlValue>}
 * with special functionality
 * relating to the tree structure
 * of TOML documents.
 * @see #create()
 * @see #copyOf(TomlTable)
 */
@ApiStatus.NonExtendable
public interface TomlTable extends TomlValue {

    /**
     * Creates an empty table
     */
    @Contract("-> new")
    static TomlTable create() {
        return new TomlTableImpl();
    }

    /**
     * Creates a new table which contains a
     * deep copy of the given table
     */
    @Contract("_ -> new")
    static TomlTable copyOf(TomlTable other) {
        return TomlTableImpl.copyOf((TomlTableImpl) other);
    }

    //

    /**
     * <p>
     *     Reports the number of <strong>entries</strong> in this map. This is not the same as
     *     the number of mappings. For instance, adding an empty map to a map
     *     will not cause the size of the map to increase.
     * </p>
     * <p>
     *     This is defined to be identical to {@code keys().size()}.
     * </p>
     * @see #keys()
     */
    @Contract(pure = true)
    int size();

    /**
     * @return True if the table is empty
     */
    boolean isEmpty();

    /**
     * Clears the table
     */
    void clear();

    /**
     * Reports the keys in this table in lexicographical order.
     * @param deep If true, children will be traversed (as in {@link #keys()}). Otherwise, only
     *             the top-level keys are reported with each having a length of 1.
     */
    @Unmodifiable Set<TomlKey> keys(boolean deep);

    /**
     * Reports the keys present in this table recursively in lexicographical order.
     * Keys that map to tables are not included.
     * @see #keys(boolean)
     */
    default @Unmodifiable Set<TomlKey> keys() {
        return this.keys(true);
    }

    /**
     * Returns true if the given key has a mapping within this table.
     * This will return true for keys mapped to tables, including empty tables.
     */
    boolean contains(TomlKey key);

    /**
     * Returns true if the given key has a mapping within this table.
     * This will return true for keys mapped to tables, including empty tables.
     * The key is parsed as specified by {@link TomlKey#parse(CharSequence)}.
     * @see #contains(TomlKey)
     */
    default boolean contains(CharSequence key) {
        return this.contains(TomlKey.parse(key));
    }

    /**
     * Gets the value mapped to the given key, or null
     * if no entry exists.
     */
    @Nullable TomlValue get(TomlKey key);

    /**
     * Gets the value mapped to the given key, or null
     * if no entry exists. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @see #get(TomlKey)
     */
    default @Nullable TomlValue get(CharSequence key) {
        return this.get(TomlKey.parse(key));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    @Nullable TomlValue put(TomlKey key, TomlValue value);

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @see #put(TomlKey, TomlValue)
     */
    default @Nullable TomlValue put(CharSequence key, TomlValue value) {
        return this.put(TomlKey.parse(key), value);
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     */
    default @Nullable TomlValue put(TomlKey key, String value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     * @throws io.github.wasabithumb.jtoml.except.TomlValueException The provided value is not representable as a TOML primitive
     */
    default @Nullable TomlValue put(TomlKey key, OffsetDateTime value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     * @throws io.github.wasabithumb.jtoml.except.TomlValueException The provided value is not representable as a TOML primitive
     */
    default @Nullable TomlValue put(TomlKey key, LocalDateTime value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     * @throws io.github.wasabithumb.jtoml.except.TomlValueException The provided value is not representable as a TOML primitive
     */
    default @Nullable TomlValue put(TomlKey key, LocalDate value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     */
    default @Nullable TomlValue put(TomlKey key, LocalTime value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(TomlKey key, boolean value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(TomlKey key, long value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(TomlKey key, int value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(TomlKey key, double value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(TomlKey key, float value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     * @see #put(TomlKey, String)
     */
    default @Nullable TomlValue put(CharSequence key, String value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @see #put(TomlKey, boolean)
     */
    default @Nullable TomlValue put(CharSequence key, boolean value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @see #put(TomlKey, long)
     */
    default @Nullable TomlValue put(CharSequence key, long value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @see #put(TomlKey, int)
     */
    default @Nullable TomlValue put(CharSequence key, int value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @see #put(TomlKey, double)
     */
    default @Nullable TomlValue put(CharSequence key, double value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @see #put(TomlKey, float)
     */
    default @Nullable TomlValue put(CharSequence key, float value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     * @throws io.github.wasabithumb.jtoml.except.TomlValueException The provided value is not representable as a TOML primitive
     * @see #put(TomlKey, OffsetDateTime)
     */
    default @Nullable TomlValue put(CharSequence key, OffsetDateTime value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     * @throws io.github.wasabithumb.jtoml.except.TomlValueException The provided value is not representable as a TOML primitive
     * @see #put(TomlKey, LocalDateTime)
     */
    default @Nullable TomlValue put(CharSequence key, LocalDateTime value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     * @throws io.github.wasabithumb.jtoml.except.TomlValueException The provided value is not representable as a TOML primitive
     * @see #put(TomlKey, LocalDate)
     */
    default @Nullable TomlValue put(CharSequence key, LocalDate value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @throws NullPointerException The value is null
     * @see #put(TomlKey, LocalTime)
     */
    default @Nullable TomlValue put(CharSequence key, LocalTime value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Removes the entry associated with the given key.
     * @return The value previously mapped to the given key, or null if no entry exists.
     */
    @Nullable TomlValue remove(TomlKey key);

    /**
     * Removes the entry associated with the given key.
     * The key is parsed as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null if no entry exists.
     * @see #remove(TomlKey)
     */
    default @Nullable TomlValue remove(String key) {
        return this.remove(TomlKey.parse(key));
    }

    /**
     * Creates a new map which contains a flattened view of this
     * table. The resulting table is ordered arbitrarily.
     */
    @Contract("-> new")
    default Map<TomlKey, TomlValue> toMap() {
        Set<TomlKey> keys = this.keys();
        Map<TomlKey, TomlValue> map = new HashMap<>(keys.size());
        for (TomlKey key : keys) {
            TomlValue value = this.get(key);
            if (value == null) throw new ConcurrentModificationException();
            map.put(key, value);
        }
        return map;
    }

}
