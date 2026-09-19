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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * An unmodifiable list of
 * {@link TomlIssue}s.
 */
@ApiStatus.AvailableSince("1.7.0")
@ApiStatus.NonExtendable
@Unmodifiable
public interface TomlIssues extends List<TomlIssue> {

    @Contract(pure = true)
    static @NotNull TomlIssues empty() {
        return TomlIssuesImpl.EMPTY;
    }

    @Contract("-> new")
    static @NotNull Builder builder() {
        return new TomlIssuesImpl.Builder();
    }

    //

    /**
     * If empty, does nothing. If there is 1
     * issue, {@link TomlIssue#unwrap() unwraps} that
     * issue. Otherwise, throws a new
     * {@link io.github.wasabithumb.jtoml.except.parse.TomlMultiParseException TomlMultiParseException}
     * representing the union of all issues.
     */
    void unwrap() throws TomlException;

    //

    @ApiStatus.NonExtendable
    interface Builder {

        @Contract("_ -> this")
        @NotNull Builder add(@NotNull TomlIssue issue);

        @Contract("_, _, _ -> this")
        default @NotNull Builder add(int line, int column, @NotNull String message) {
            return this.add(TomlIssue.issue(line, column, message));
        }

        @Contract("_ -> this")
        default @NotNull Builder add(@NotNull TomlLocalParseException exception) {
            return this.add(TomlIssue.issue(exception));
        }

        @Contract("-> new")
        @NotNull TomlIssues build();

    }

}
