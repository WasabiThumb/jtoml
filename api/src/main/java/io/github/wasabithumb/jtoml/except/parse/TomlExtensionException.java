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

package io.github.wasabithumb.jtoml.except.parse;

import org.jetbrains.annotations.NotNull;

/**
 * An attempt was made to extend an inline table or array.
 * From the TOML docs:
 * <ul>
 *     <li>
 *         Inline tables are fully self-contained and define all keys and sub-tables within them.
 *         Keys and sub-tables cannot be added outside the braces.
 *     </li>
 *     <li>
 *         Attempting to append to a statically defined array, even if that array is empty,
 *         must produce an error at parse time.
 *     </li>
 * </ul>
 */
public final class TomlExtensionException extends TomlParseException {

    private static final long serialVersionUID = -7114185111042462133L;

    //

    public TomlExtensionException(@NotNull String message) {
        super(message);
    }

}
