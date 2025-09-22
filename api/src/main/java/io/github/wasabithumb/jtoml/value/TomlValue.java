package io.github.wasabithumb.jtoml.value;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A TOML {@link TomlPrimitive primitive},
 * {@link TomlArray array} or {@link TomlTable table}.
 * Explicit casts should not be performed. Instead,
 * use {@link #asPrimitive()}, {@link #asArray()}
 * or {@link #asTable()}.
 */
@ApiStatus.NonExtendable
public interface TomlValue {

    /**
     * Returns a deep copy of the provided value
     * @see TomlTable#copyOf(TomlTable)
     * @see TomlArray#copyOf(Iterable)
     * @see TomlPrimitive#copyOf(TomlPrimitive)
     */
    @Contract("_ -> new")
    @ApiStatus.AvailableSince("0.6.4")
    static @NotNull TomlValue copyOf(@NotNull TomlValue other) {
        if (other.isTable()) {
            return TomlTable.copyOf(other.asTable());
        } else if (other.isArray()) {
            return TomlArray.copyOf(other.asArray());
        } else {
            return TomlPrimitive.copyOf(other.asPrimitive());
        }
    }

    //

    /**
     * Reports the time that this object was created
     * as reported by {@link System#nanoTime()}.
     */
    long creationTime();

    /**
     * Reports the flags stored on this value.
     * Flags are opaque to consumers but have internal meaning
     * required for spec-compliant reading.
     */
    @ApiStatus.Internal
    @ApiStatus.AvailableSince("1.2.1")
    int flags();

    /**
     * Sets the flags stored on this value.
     * Flags are opaque to consumers but have internal meaning
     * required for spec-compliant reading.
     */
    @ApiStatus.Internal
    @ApiStatus.AvailableSince("1.2.1")
    @Contract("_ -> this")
    @NotNull TomlValue flags(int flags);

    /**
     * Accesses the comments stored on this value
     */
    @ApiStatus.AvailableSince("0.6.0")
    @NotNull Comments comments();

    /**
     * <p>
     *     Returns true if the value represents a {@link TomlPrimitive primitive}
     *     and {@link #asPrimitive()} may be called.
     * </p>
     * <p>
     *     The return value of this method may or may not be identical to
     *     {@code this instanceof TomlPrimitive}.
     * </p>
     */
    default boolean isPrimitive() {
        return this instanceof TomlPrimitive;
    }

    /**
     * Converts this value to a {@link TomlPrimitive primitive}.
     * Whether the method returns {@code this} or not is an implementation detail.
     * @throws UnsupportedOperationException Value does not represent a primitive (see {@link #isPrimitive()})
     */
    default @NotNull TomlPrimitive asPrimitive() throws UnsupportedOperationException {
        if (this instanceof TomlPrimitive) return (TomlPrimitive) this;
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     *     Returns true if the value represents a {@link TomlArray array}
     *     and {@link #asArray()} may be called.
     * </p>
     * <p>
     *     The return value of this method may or may not be identical to
     *     {@code this instanceof TomlArray}.
     * </p>
     */
    default boolean isArray() {
        return this instanceof TomlArray;
    }

    /**
     * Converts this value to a {@link TomlArray array}.
     * Whether the method returns {@code this} or not is an implementation detail.
     * @throws UnsupportedOperationException Value does not represent an array (see {@link #isArray()})
     */
    default @NotNull TomlArray asArray() throws UnsupportedOperationException {
        if (this instanceof TomlArray) return (TomlArray) this;
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     *     Returns true if the value represents a {@link TomlTable table}
     *     and {@link #asTable()} may be called.
     * </p>
     * <p>
     *     The return value of this method may or may not be identical to
     *     {@code this instanceof TomlTable}.
     * </p>
     */
    default boolean isTable() {
        return this instanceof TomlTable;
    }

    /**
     * Converts this value to a {@link TomlTable table}.
     * Whether the method returns {@code this} or not is an implementation detail.
     * @throws UnsupportedOperationException Value does not represent a table (see {@link #isTable()})
     */
    default @NotNull TomlTable asTable() throws UnsupportedOperationException {
        if (this instanceof TomlTable) return (TomlTable) this;
        throw new UnsupportedOperationException();
    }

}
