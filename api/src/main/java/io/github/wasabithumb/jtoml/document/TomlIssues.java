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
import io.github.wasabithumb.jtoml.util.Buildable;
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
public interface TomlIssues extends List<TomlIssue>, Buildable<TomlIssues> {

    /**
     * Reports an empty {@link TomlIssues} instance
     * containing no issues ({@link TomlIssues#size() size} is 0,
     * {@link TomlIssues#unwrap() unwrap} does nothing)
     */
    @Contract(pure = true)
    static @NotNull TomlIssues empty() {
        return TomlIssuesImpl.EMPTY;
    }

    /**
     * Creates a new builder for constructing a
     * {@link TomlIssues} list.
     */
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

    @Override
    Builder toBuilder();

    //

    /**
     * Facilitates the creation
     * of a {@link TomlIssues} list.
     * @see #add(TomlIssue)
     * @see #build()
     */
    @ApiStatus.NonExtendable
    interface Builder extends Buildable.Builder<TomlIssues> {

        /**
         * Adds a new issue to the resulting {@link TomlIssues} list.
         * @param issue The issue to add
         * @return This builder
         */
        @Contract("_ -> this")
        @NotNull Builder add(@NotNull TomlIssue issue);

        /**
         * Adds a new issue to the resulting {@link TomlIssues} list.
         * @param line The line number of the issue to add
         * @param column The column number of the issue to add
         * @param message The message of the issue to add
         * @return This builder
         */
        @Contract("_, _, _ -> this")
        default @NotNull Builder add(int line, int column, @NotNull String message) {
            return this.add(TomlIssue.issue(line, column, message));
        }

        /**
         * Adds a new issue to the resulting {@link TomlIssues} list.
         * @param exception The exception from which to generate the issue to add
         *                  (via {@link TomlIssue#issue(TomlLocalParseException) TomlIssue#issue}).
         * @return This builder
         */
        @Contract("_ -> this")
        default @NotNull Builder add(@NotNull TomlLocalParseException exception) {
            return this.add(TomlIssue.issue(exception));
        }

        /**
         * Creates a new {@link TomlIssues} instance
         * reflecting the internal state of the builder.
         * Future usage of this builder will not mutate
         * this resultant object.
         * @return A new {@link TomlIssues} object
         */
        @Override
        @Contract("-> new")
        @NotNull TomlIssues build();

    }

}
