package io.github.wasabithumb.jtoml.except.parse;

import org.jetbrains.annotations.NotNull;

/**
 * Failed to parse due to clobbering; a key or table was redefined either
 * explicitly or in an attempt to use an existing key as a table.
 */
public final class TomlClobberException extends TomlParseException {

    public TomlClobberException(@NotNull String message) {
        super(message);
    }

}
