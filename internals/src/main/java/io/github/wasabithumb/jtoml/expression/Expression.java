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

package io.github.wasabithumb.jtoml.expression;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Expression {

    static @NotNull EmptyExpression empty() {
        return new EmptyExpression();
    }

    static @NotNull KeyValueExpression keyValue(@NotNull TomlKey key, @NotNull TomlValue value) {
        return new KeyValueExpression(key, value);
    }

    static @NotNull TableExpression table(@NotNull TomlKey key, boolean isArray) {
        return new TableExpression(key, isArray);
    }

    //

    @Nullable String getComment();

    void setComment(@Nullable String comment);

    default boolean isEmpty() {
        return false;
    }

    @Contract("-> this")
    default @NotNull EmptyExpression asEmpty() {
        return (EmptyExpression) this;
    }

    default boolean isKeyValue() {
        return false;
    }

    @Contract("-> this")
    default @NotNull KeyValueExpression asKeyValue() {
        return (KeyValueExpression) this;
    }

    default boolean isTable() {
        return false;
    }

    @Contract("-> this")
    default @NotNull TableExpression asTable() {
        return (TableExpression) this;
    }

}
