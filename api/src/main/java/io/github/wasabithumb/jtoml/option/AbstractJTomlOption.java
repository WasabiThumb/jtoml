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

package io.github.wasabithumb.jtoml.option;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
abstract class AbstractJTomlOption<T> implements JTomlOption<T> {

    private static int HEAD = 0;
    private static synchronized int nextOrdinal() {
        return HEAD++;
    }

    //

    protected final int ordinal;
    protected final String name;
    protected final T defaultValue;

    protected AbstractJTomlOption(@NotNull String name, @NotNull T defaultValue) {
        this.ordinal = nextOrdinal();
        this.name = name;
        this.defaultValue = defaultValue;
    }

    //

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull T defaultValue() {
        return this.defaultValue;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.ordinal());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JTomlOption<?>)) return false;
        return this.ordinal() == ((JTomlOption<?>) obj).ordinal();
    }

    @Override
    public @NotNull String toString() {
        return this.name();
    }

}
