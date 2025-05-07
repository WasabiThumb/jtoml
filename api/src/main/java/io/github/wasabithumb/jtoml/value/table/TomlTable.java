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

    @Contract("-> new")
    static @NotNull TomlTable create() {
        return new TomlTableImpl();
    }

    @Contract("_ -> new")
    static @NotNull TomlTable copyOf(@NotNull TomlTable other) {
        TomlTableImpl ret = new TomlTableImpl();
        ret.putAll((TomlTableImpl) other);
        return ret;
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
     * Reports the keys in this table
     * @param deep If true, children will be traversed (as in {@link #keys()}). Otherwise,
     *             the top-level keys are reported; each having a length of 1
     */
    @NotNull @Unmodifiable Set<TomlKey> keys(boolean deep);

    /**
     * Reports the keys present in this table recursively.
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

    @Nullable TomlValue get(@NotNull TomlKey key);

    default @Nullable TomlValue get(@NotNull CharSequence key) {
        return this.get(TomlKey.parse(key));
    }

    @Nullable TomlValue put(@NotNull TomlKey key, @NotNull TomlValue value);

    default @Nullable TomlValue put(@NotNull CharSequence key, @NotNull TomlValue value) {
        return this.put(TomlKey.parse(key), value);
    }

    default @Nullable TomlValue put(@NotNull TomlKey key, @NotNull String value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull TomlKey key, boolean value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull TomlKey key, long value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull TomlKey key, int value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull TomlKey key, double value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull TomlKey key, float value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull CharSequence key, @NotNull String value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull CharSequence key, boolean value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull CharSequence key, long value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull CharSequence key, int value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull CharSequence key, double value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    default @Nullable TomlValue put(@NotNull CharSequence key, float value) {
        return this.put(key, TomlPrimitive.of(value));
    }

    @Nullable TomlValue remove(@NotNull TomlKey key);

    default @Nullable TomlValue remove(@NotNull String key) {
        return this.remove(TomlKey.parse(key));
    }

    @Contract("-> new")
    default @NotNull Map<TomlKey, TomlValue> toMap() {
        Set<TomlKey> keys = this.keys();
        Map<TomlKey, TomlValue> map = new HashMap<>(keys.size());
        for (TomlKey key : keys) map.put(key, this.get(key));
        return map;
    }

}
