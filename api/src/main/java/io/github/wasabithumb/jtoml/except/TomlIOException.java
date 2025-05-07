package io.github.wasabithumb.jtoml.except;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**
 * Wrapper for {@link IOException}
 */
public final class TomlIOException extends TomlException {

    @ApiStatus.Internal
    @Contract("_ -> fail")
    public static void rethrow(@NotNull IOException cause) {
        throw new TomlIOException("Generic IO exception", cause);
    }

    //

    @Contract("_, null -> fail")
    public TomlIOException(@NotNull String message, IOException cause) {
        super(message, Objects.requireNonNull(cause));
    }

    //

    @Override
    public @NotNull IOException getCause() {
        return (IOException) super.getCause();
    }

    @Contract("-> fail")
    public void unwrap() throws IOException {
        throw this.getCause();
    }

}
