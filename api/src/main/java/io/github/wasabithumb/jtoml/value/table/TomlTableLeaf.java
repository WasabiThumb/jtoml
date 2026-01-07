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

package io.github.wasabithumb.jtoml.value.table;

import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class TomlTableLeaf implements TomlTableNode {

    private final TomlValue value;

    TomlTableLeaf(@NotNull TomlValue value) {
        this.value = value;
    }

    //

    public @NotNull TomlValue value() {
        return this.value;
    }

    // START Node Super


    @Override
    public int entryCount() {
        return 1;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    @Contract("-> fail")
    public @NotNull TomlTableBranch asBranch() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    @Contract("-> this")
    public @NotNull TomlTableLeaf asLeaf() {
        return this;
    }

    // END Node Super

}
