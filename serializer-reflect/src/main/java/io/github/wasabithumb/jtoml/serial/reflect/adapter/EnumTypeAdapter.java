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
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class EnumTypeAdapter<T extends Enum<T>> implements TypeAdapter<T> {

    private final Class<T> typeClass;

    EnumTypeAdapter(Class<T> typeClass) {
        this.typeClass = typeClass;
    }

    //

    @Override
    public @NotNull Class<T> typeClass() {
        return this.typeClass;
    }

    @Override
    public @NotNull T toJava(@NotNull TomlValue toml) {
        return Enum.valueOf(this.typeClass, toml.asPrimitive().asString());
    }

    @Override
    public @NotNull TomlValue toToml(@NotNull T java) {
        return TomlPrimitive.of(java.name());
    }

}
