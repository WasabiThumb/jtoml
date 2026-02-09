package io.github.wasabithumb.jtoml.key.convention;

import io.github.wasabithumb.jtoml.key.TomlKey;
import org.jetbrains.annotations.NotNull;

/**
 * Determines how a given identifier may be
 * converted to a {@link TomlKey}. This is currently
 * used by the reflect serializer to map fields and
 * record components to their corresponding keys in
 * absence of an explicit mapping. Owing to the general
 * use cases of this interface, the
 * {@link StandardKeyConvention standard conventions}
 * appropriately expect input to be strict ASCII in
 * {@code camelCase}.
 * @see StandardKeyConvention
 */
@FunctionalInterface
public interface KeyConvention {

    /**
     * Adapts the given identifier into a {@link TomlKey}
     * based on the convention.
     */
    @NotNull TomlKey toToml(@NotNull String key);
    
}
