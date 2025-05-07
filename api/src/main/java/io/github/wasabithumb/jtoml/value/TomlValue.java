package io.github.wasabithumb.jtoml.value;

import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
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

    default boolean isPrimitive() {
        return this instanceof TomlPrimitive;
    }

    default @NotNull TomlPrimitive asPrimitive() throws UnsupportedOperationException {
        if (this instanceof TomlPrimitive) return (TomlPrimitive) this;
        throw new UnsupportedOperationException();
    }

    default boolean isArray() {
        return this instanceof TomlArray;
    }

    default @NotNull TomlArray asArray() throws UnsupportedOperationException {
        if (this instanceof TomlArray) return (TomlArray) this;
        throw new UnsupportedOperationException();
    }

    default boolean isTable() {
        return this instanceof TomlTable;
    }

    default @NotNull TomlTable asTable() throws UnsupportedOperationException {
        if (this instanceof TomlTable) return (TomlTable) this;
        throw new UnsupportedOperationException();
    }

}
