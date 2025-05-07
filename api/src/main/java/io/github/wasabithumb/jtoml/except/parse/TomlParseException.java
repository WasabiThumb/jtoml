package io.github.wasabithumb.jtoml.except.parse;

import io.github.wasabithumb.jtoml.except.TomlException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Errors relating to TOML parsing
 */
@ApiStatus.NonExtendable
public abstract class TomlParseException extends TomlException {

    public TomlParseException(@NotNull String message) {
        super(message);
    }

    public TomlParseException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
