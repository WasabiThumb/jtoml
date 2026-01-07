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

package io.github.wasabithumb.jtoml.value.table;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TOML key-values
 * @see #create()
 * @see #copyOf(TomlTable)
 */
@ApiStatus.NonExtendable
public interface TomlTable extends TomlValue {

    /**
     * Creates an empty table
     */
    @Contract("-> new")
    static @NotNull TomlTable create() {
        return new TomlTableImpl();
    }

    /**
     * Creates a new table which contains a
     * deep copy of the given table
     */
    @Contract("_ -> new")
    static @NotNull TomlTable copyOf(@NotNull TomlTable other) {
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
     * @param deep If true, children will be traversed (as in {@link #keys()}). Otherwise only
     *             the top-level keys are reported, each having a length of 1.
     */
    @NotNull @Unmodifiable Set<TomlKey> keys(boolean deep);

    /**
     * Reports the keys present in this table recursively in lexicographical order.
     * Keys that map to tables are not included.
     * @see #keys(boolean)
     */
    default @NotNull @Unmodifiable Set<TomlKey> keys() {
        return this.keys(true);
    }

    /**
     * Returns true if the given key has a mapping within this table.
     * This will return true for keys mapped to tables, including empty tables.
     */
    boolean contains(@NotNull TomlKey key);

    /**
     * Returns true if the given key has a mapping within this table.
     * This will return true for keys mapped to tables, including empty tables.
     * The key is parsed as specified by {@link TomlKey#parse(CharSequence)}.
     * @see #contains(TomlKey)
     */
    default boolean contains(@NotNull CharSequence key) {
        return this.contains(TomlKey.parse(key));
    }

    /**
     * Gets the value mapped to the given key, or null
     * if no entry exists.
     */
    @Nullable TomlValue get(@NotNull TomlKey key);

    /**
     * Gets the value mapped to the given key, or null
     * if no entry exists. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @see #get(TomlKey)
     */
    default @Nullable TomlValue get(@NotNull CharSequence key) {
        return this.get(TomlKey.parse(key));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    @Nullable TomlValue put(@NotNull TomlKey key, @NotNull TomlValue value);

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @see #put(TomlKey, TomlValue)
     */
    default @Nullable TomlValue put(@NotNull CharSequence key, @NotNull TomlValue value) {
        return this.put(TomlKey.parse(key), value);
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(@NotNull TomlKey key, @NotNull String value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(@NotNull TomlKey key, boolean value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(@NotNull TomlKey key, long value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(@NotNull TomlKey key, int value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(@NotNull TomlKey key, double value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map.
     * @return The value previously mapped to the given key, or null the entry was newly created
     */
    default @Nullable TomlValue put(@NotNull TomlKey key, float value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Updates the value mapped to the given key, creating a
     * new entry if one does not exist.
     * The value is wrapped into a {@link TomlPrimitive}
     * before being placed into the map. The key is parsed
     * as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null the entry was newly created
     * @see #put(TomlKey, String)
     */
    default @Nullable TomlValue put(@NotNull CharSequence key, @NotNull String value) {
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
    default @Nullable TomlValue put(@NotNull CharSequence key, boolean value) {
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
    default @Nullable TomlValue put(@NotNull CharSequence key, long value) {
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
    default @Nullable TomlValue put(@NotNull CharSequence key, int value) {
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
    default @Nullable TomlValue put(@NotNull CharSequence key, double value) {
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
    default @Nullable TomlValue put(@NotNull CharSequence key, float value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    /**
     * Removes the entry associated with the given key.
     * @return The value previously mapped to the given key, or null if no entry exists.
     */
    @Nullable TomlValue remove(@NotNull TomlKey key);

    /**
     * Removes the entry associated with the given key.
     * The key is parsed as specified by {@link TomlKey#parse(CharSequence)}.
     * @return The value previously mapped to the given key, or null if no entry exists.
     * @see #remove(TomlKey)
     */
    default @Nullable TomlValue remove(@NotNull String key) {
        return this.remove(TomlKey.parse(key));
    }

    /**
     * Creates a new map which contains a flattened view of this
     * table. The resulting table is ordered arbitrarily.
     */
    @Contract("-> new")
    default @NotNull Map<TomlKey, TomlValue> toMap() {
        Set<TomlKey> keys = this.keys();
        Map<TomlKey, TomlValue> map = new HashMap<>(keys.size());
        for (TomlKey key : keys) map.put(key, this.get(key));
        return map;
    }

}
