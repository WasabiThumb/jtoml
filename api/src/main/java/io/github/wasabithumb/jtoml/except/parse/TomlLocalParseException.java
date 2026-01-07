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
import org.jetbrains.annotations.Nullable;

/**
 * A {@link TomlParseException} with location info
 */
public final class TomlLocalParseException extends TomlParseException {

    private static final long serialVersionUID = -4596811463003519497L;

    //

    private final int lineNumber;
    private final int columnNumber;

    public TomlLocalParseException(@NotNull String message, int lineNumber, int columnNumber) {
        super(message);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public TomlLocalParseException(@NotNull String message, @Nullable Throwable cause, int lineNumber, int columnNumber) {
        super(message, cause);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    //

    public @NotNull String getRawMessage() {
        return super.getMessage();
    }

    @Override
    public @NotNull String getMessage() {
        return super.getMessage() + " @ " + this.lineNumber + ":" + this.columnNumber;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getColumnNumber() {
        return this.columnNumber;
    }

}
