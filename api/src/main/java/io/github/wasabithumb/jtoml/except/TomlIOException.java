/*
 * Copyright 2026 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wasabithumb.jtoml.except;

import io.github.wasabithumb.jtoml.except.parse.TomlCodingException;
import io.github.wasabithumb.jtoml.except.parse.TomlTruncatedException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.Objects;

/**
 * Wrapper for {@link IOException}. This should only be thrown
 * when an issue arises in the underlying stream <i>not</i>
 * related to parsing. The {@link #rethrow(IOException) rethrow} utility
 * will only generate a {@link TomlIOException} if the provided
 * {@link IOException} is not a {@link CharacterCodingException}
 * (generates {@link io.github.wasabithumb.jtoml.except.parse.TomlCodingException TomlCodingException}),
 * and is not an {@link EOFException}
 * (generates {@link io.github.wasabithumb.jtoml.except.parse.TomlTruncatedException TomlTruncatedException}).
 */
public final class TomlIOException extends TomlException {

    private static final long serialVersionUID = -4876355186588461257L;

    /**
     * A utility for handling low-level {@link IOException}s, throwing
     * the appropriate {@link TomlException} (which is not always {@link TomlIOException}).
     * Not meant for use outside of library internals.
     */
    @ApiStatus.Internal
    @Contract("_ -> fail")
    public static void rethrow(@NotNull IOException cause) throws TomlException {
        if (cause instanceof EOFException) {
            throw new TomlTruncatedException(
                    "Unexpected end of document",
                    (EOFException) cause
            );
        } else if (cause instanceof CharacterCodingException) {
            throw new TomlCodingException(
                    "Underlying reader failed to encode/decode text (is the source and reader UTF-8?)",
                    (CharacterCodingException) cause
            );
        } else {
            throw new TomlIOException(
                    "Underlying stream raised an exception",
                    cause
            );
        }
    }

    //

    @Contract("_, null -> fail")
    private TomlIOException(@NotNull String message, IOException cause) {
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
