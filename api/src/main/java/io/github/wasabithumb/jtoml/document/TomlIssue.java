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

package io.github.wasabithumb.jtoml.document;

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.parse.TomlLocalParseException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * A silenced parsing error stored on a {@link TomlDocument document}
 * when {@link io.github.wasabithumb.jtoml.option.JTomlOption#ERROR_RECOVERY error recovery}
 * is enabled.
 */
@ApiStatus.AvailableSince("1.7.0")
@ApiStatus.NonExtendable
public interface TomlIssue {

    /**
     * Creates an issue wrapping the given
     * exception.
     * @throws NullPointerException exception is null
     */
    static TomlIssue issue(TomlLocalParseException exception) {
        Objects.requireNonNull(exception, "exception must not be null");
        return new TomlIssueImpl(exception.getLineNumber(), exception.getColumnNumber(), exception.getRawMessage());
    }

    /**
     * Creates a new issue.
     * @param line Line number of the issue (starting from 0)
     * @param column Column number of the issue (starting from 0)
     * @param message Message string of the issue
     * @throws IllegalStateException line or column is less than 0
     * @throws NullPointerException message is null
     */
    static TomlIssue issue(int line, int column, String message) {
        if (line < 0) throw new IllegalStateException("line must not be negative");
        if (column < 0) throw new IllegalStateException("column must not be negative");
        return new TomlIssueImpl(
                line,
                column,
                Objects.requireNonNull(message, "message must not be null")
        );
    }

    //

    /**
     * Reports the line at which this issue was encountered,
     * starting from 0.
     */
    @Contract(pure = true)
    int line();

    /**
     * Reports the column at which this issue was encountered,
     * starting from 0. This is measured in Java (UTF-16) characters.
     */
    @Contract(pure = true)
    int column();

    /**
     * Reports a human-readable description of the issue encountered.
     */
    @Contract(pure = true)
    String message();

    /**
     * Creates a new {@link TomlLocalParseException}
     * with the same information as stored in this
     * issue.
     * @see #unwrap()
     */
    @Contract("-> new")
    default TomlLocalParseException toException() {
        return new TomlLocalParseException(this.message(), this.line(), this.column());
    }

    /**
     * Throws the exception yielded by
     * {@link #toException()}.
     */
    @Contract("-> fail")
    default void unwrap() throws TomlException {
        throw this.toException();
    }

}
