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
import org.jetbrains.annotations.NotNull;

public final class TableExpression extends AbstractExpression {

    private final TomlKey key;
    private final boolean isArray;

    public TableExpression(@NotNull TomlKey key, boolean isArray) {
        this.key = key;
        this.isArray = isArray;
    }

    //

    public @NotNull TomlKey key() {
        return this.key;
    }

    public boolean isArray() {
        return this.isArray;
    }

    @Override
    public boolean isTable() {
        return true;
    }

}
