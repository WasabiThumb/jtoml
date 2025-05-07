package io.github.wasabithumb.jtoml.except;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Superclass of exceptions pertaining to TOML
 * parsing, serialization and writing.
 * Notably includes {@link TomlIOException} which
 * should be explicitly caught when appropriate.
 */
@ApiStatus.NonExtendable
public abstract class TomlException extends RuntimeException {

    public TomlException(@NotNull String message) {
        super(message);
    }

    public TomlException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
