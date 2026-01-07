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

    private static final long serialVersionUID = 4172247378895056624L;

    //

    public TomlException(@NotNull String message) {
        super(message);
    }

    public TomlException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
