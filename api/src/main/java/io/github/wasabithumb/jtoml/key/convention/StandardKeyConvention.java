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

package io.github.wasabithumb.jtoml.key.convention;

import io.github.wasabithumb.jtoml.key.TomlKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Enumeration of standard {@link KeyConvention} implementations.
 * As an enum, these may be used in the {@code @Convention} annotation.
 *
 * <table>
 *     <caption>Comparison</caption>
 *     <tr>
 *         <th>{@link #LITERAL}</th>
 *         <th>{@link #LOWER}</th>
 *         <th>{@link #KEBAB}</th>
 *         <th>{@link #SNAKE}</th>
 *         <th>{@link #SPLIT}</th>
 *     </tr>
 *     <tr>
 *         <td><pre>{@code helloWorld}</pre></td>
 *         <td><pre>{@code helloworld}</pre></td>
 *         <td><pre>{@code hello-world}</pre></td>
 *         <td><pre>{@code hello_world}</pre></td>
 *         <td><pre>{@code hello.world}</pre></td>
 *     </tr>
 * </table>
 *
 * @see #LITERAL
 * @see #LOWER
 * @see #KEBAB
 * @see #SNAKE
 * @see #SPLIT
 */
@ApiStatus.AvailableSince("1.5.0")
public enum StandardKeyConvention implements KeyConvention {

    /**
     * Produces a {@link TomlKey} consisting
     * of a single part equal to the given identifier
     */
    LITERAL(KeyConventionOps::identity),

    /**
     * Produces a {@link TomlKey} consisting
     * of a single part equal to the given identifier in lowercase
     */
    LOWER(KeyConventionOps::lower),

    /**
     * Produces a {@link TomlKey} consisting
     * of a single part based on the given identifier.
     * To create this part, the identifier is lowercased
     * and the character {@code -} is placed before runs
     * of characters which were uppercase.
     */
    KEBAB(KeyConventionOps::camelToKebab),

    /**
     * Produces a {@link TomlKey} consisting
     * of a single part based on the given identifier.
     * To create this part, the identifier is lowercased
     * and the character {@code _} is placed before runs
     * of characters which were uppercase.
     */
    SNAKE(KeyConventionOps::camelToSnake),

    /**
     * Produces a {@link TomlKey} consisting
     * of one or more parts based on the given identifier.
     * The identifier is split by the start of any run
     * of uppercase characters, between which a part is
     * generated from the lowercase value of that text.
     * @apiNote Behavior is not well tested at this time.
     */
    @ApiStatus.Experimental
    SPLIT(KeyConventionOps::camelToSplit);

    //

    private final Function<String, TomlKey> operator;

    StandardKeyConvention(Function<String, TomlKey> operator) {
        this.operator = operator;
    }

    //

    @Override
    public @NotNull TomlKey toToml(@NotNull String key) {
        return this.operator.apply(key);
    }
    
}
