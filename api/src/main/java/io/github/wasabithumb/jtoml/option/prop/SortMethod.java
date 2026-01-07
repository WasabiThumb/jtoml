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

package io.github.wasabithumb.jtoml.option.prop;

import org.jetbrains.annotations.ApiStatus;

/**
 * Determines how keys within a table
 * are sorted when writing.
 * @see #STRATIFIED
 * @see #LEXICOGRAPHICAL
 * @see #TIME
 */
@ApiStatus.AvailableSince("1.3.0")
public enum SortMethod {
    /**
     * Groups keys by value type: first primitives,
     * then arrays, then arrays-of-tables, then tables.
     * This ensures that arrays-of-tables and tables are
     * never forced to use inline syntax.
     */
    STRATIFIED,

    /**
     * Keys are sorted by identity, resulting
     * in a lexicographical order.
     * This may force arrays-of-tables and tables to use inline
     * syntax in order to produce valid TOML.
     */
    LEXICOGRAPHICAL,

    /**
     * Keys are sorted by creation time.
     * This may force arrays-of-tables and tables to use inline
     * syntax in order to produce valid TOML.
     */
    TIME;
}
