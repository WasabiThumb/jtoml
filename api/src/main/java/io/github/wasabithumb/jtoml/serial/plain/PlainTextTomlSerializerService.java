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

package io.github.wasabithumb.jtoml.serial.plain;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.serial.TomlSerializerService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class PlainTextTomlSerializerService extends TomlSerializerService {

    @Override
    public boolean canSerializeTo(@NotNull Class<?> outType) {
        return String.class.equals(outType);
    }

    @Override
    public boolean canDeserializeFrom(@NotNull Class<?> inType) {
        return String.class.equals(inType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> TomlSerializer<?, T> getSerializer(@NotNull JToml instance, @NotNull Class<T> outType) {
        if (!String.class.equals(outType)) throw new IllegalArgumentException();
        return (TomlSerializer<?, T>) new PlainTextTomlSerializer(instance);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> TomlSerializer<T, ?> getDeserializer(@NotNull JToml instance, @NotNull Class<T> inType) {
        if (!String.class.equals(inType)) throw new IllegalArgumentException();
        return (TomlSerializer<T, ?>) new PlainTextTomlSerializer(instance);
    }

}
