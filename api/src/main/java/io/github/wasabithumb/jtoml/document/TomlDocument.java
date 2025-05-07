package io.github.wasabithumb.jtoml.document;

import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;

/**
 * The top-level table read from a TOML file/string.
 * Provides no additional API.
 */
@ApiStatus.NonExtendable
public interface TomlDocument extends TomlTable { }
