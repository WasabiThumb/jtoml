package io.github.wasabithumb.jtoml.serial;

import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 *     An abstraction for facets which can transform
 *     TOML tables to/from some other type in-memory.
 *     Useful for digesting and converting configuration
 *     files.
 * </p>
 * <p>
 *     No serializers are present in the base JToml artifact,
 *     they are separate dependencies.
 * </p>
 */
public interface TomlSerializer<I, O> {

    @ApiStatus.OverrideOnly
    @NotNull Class<I> inType();

    @ApiStatus.OverrideOnly
    @NotNull Class<O> outType();

    @NotNull O serialize(@NotNull TomlTable table);

    @NotNull TomlTable deserialize(@NotNull I data);

    //

    /**
     * A {@link TomlSerializer} which serializes and deserializes
     * the same type
     */
    interface Symmetric<T> extends TomlSerializer<T, T> {

        @ApiStatus.OverrideOnly
        @NotNull Class<T> serialType();

        @Override
        default @NotNull Class<T> inType() {
            return this.serialType();
        }

        @Override
        default @NotNull Class<T> outType() {
            return this.serialType();
        }

    }

}
