package io.github.wasabithumb.jtoml.except.parse;

import org.jetbrains.annotations.NotNull;

import java.time.DateTimeException;

/**
 * Wraps a {@link DateTimeException}; thrown when trying
 * to parse an invalid datetime
 */
public final class TomlDateTimeException extends TomlParseException {

    public TomlDateTimeException(@NotNull DateTimeException cause) {
        super("Invalid datetime", cause);
    }

    @Override
    public @NotNull DateTimeException getCause() {
        return (DateTimeException) super.getCause();
    }

}
