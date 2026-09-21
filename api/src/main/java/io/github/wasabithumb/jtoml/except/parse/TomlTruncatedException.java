package io.github.wasabithumb.jtoml.except.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;

/**
 * Raised when end of document is reached
 * while parsing a symbol such that the symbol is left
 * incomplete. An incomplete string literal
 * (e.g. {@code "hello}) is one such case. This exception
 * may or may not be wrapping an underlying
 * {@link EOFException}, specifically
 * to avoid throwing the harsher
 * {@link io.github.wasabithumb.jtoml.except.TomlIOException TomlIOException}
 * which is not to be treated as an issue with the underlying document.
 */
public final class TomlTruncatedException extends TomlParseException {

    private static final long serialVersionUID = 4875135992233471425L;

    //

    public TomlTruncatedException(@NotNull String message) {
        super(message);
    }

    public TomlTruncatedException(@NotNull String message, @Nullable EOFException cause) {
        super(message, cause);
    }

    //


    @Override
    public @Nullable EOFException getCause() {
        return (EOFException) super.getCause();
    }

}
