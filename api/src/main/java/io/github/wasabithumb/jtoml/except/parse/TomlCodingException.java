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

package io.github.wasabithumb.jtoml.except.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.CharacterCodingException;

/**
 * Error while decoding UTF-8 data.
 * May be wrapping an underlying {@link CharacterCodingException}
 * when parsing from a {@link java.io.Reader Reader}.
 */
public final class TomlCodingException extends TomlParseException {

    private static final long serialVersionUID = 7638026155229665324L;

    //

    public TomlCodingException(@NotNull String message) {
        super(message);
    }

    public TomlCodingException(@NotNull String message, @Nullable CharacterCodingException cause) {
        super(message, cause);
    }

    //

    @Override
    public @Nullable CharacterCodingException getCause() {
        return (CharacterCodingException) super.getCause();
    }

}
