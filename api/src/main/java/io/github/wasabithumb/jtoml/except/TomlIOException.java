/*
 * Copyright 2025 Xavier Pedraza
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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**
 * Wrapper for {@link IOException}
 */
public final class TomlIOException extends TomlException {

    private static final long serialVersionUID = -4876355186588461257L;

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
