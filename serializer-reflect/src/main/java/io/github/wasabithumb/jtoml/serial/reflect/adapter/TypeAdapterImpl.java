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

package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@ApiStatus.Internal
final class TypeAdapterImpl<T> implements TypeAdapter<T> {

    private final Class<T> typeClass;
    private final Function<TomlValue, T> toJava;
    private final Function<T, TomlValue> toToml;

    TypeAdapterImpl(
            @NotNull Class<T> typeClass,
            @NotNull Function<TomlValue, T> toJava,
            @NotNull Function<T, TomlValue> toToml
    ) {
        this.typeClass = typeClass;
        this.toJava = toJava;
        this.toToml = toToml;
    }

    //

    @Override
    public @NotNull Class<T> typeClass() {
        return this.typeClass;
    }

    @Override
    public @NotNull T toJava(@NotNull TomlValue toml) {
        return this.toJava.apply(toml);
    }

    @Override
    public @NotNull TomlValue toToml(@NotNull T java) {
        return this.toToml.apply(java);
    }

}
