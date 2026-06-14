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

package io.github.wasabithumb.jtoml.serial.reflect;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.serial.TomlSerializerFactory;
import io.github.wasabithumb.jtoml.serial.reflect.adapter.TypeAdapters;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class ReflectTomlSerializerFactory extends TomlSerializerFactory {

    @Override
    public @NotNull <T> Result<?, T> fromToml(@NotNull JToml instance, @NotNull Class<T> outType) {
        int features = features(instance) | ReflectTomlSerializer.Feature.SUPPORTS_FROM_TOML;
        String error = ReflectTomlSerializer.anyTypeError(outType, features);
        if (error != null) return Result.invalid(error);
        return Result.valid(() -> new ReflectTomlSerializer<>(
                outType,
                TypeAdapters.standard(),
                instance.options().get(JTomlOption.DEFAULT_KEY_CONVENTION),
                features
        ));
    }

    @Override
    public @NotNull <T> Result<T, ?> toToml(@NotNull JToml instance, @NotNull Class<T> inType) {
        int features = features(instance) | ReflectTomlSerializer.Feature.SUPPORTS_TO_TOML;
        String error = ReflectTomlSerializer.anyTypeError(inType, features);
        if (error != null) return Result.invalid(error);
        return Result.valid(() -> new ReflectTomlSerializer<>(
                inType,
                TypeAdapters.standard(),
                instance.options().get(JTomlOption.DEFAULT_KEY_CONVENTION),
                features
        ));
    }

    //

    private static @ReflectTomlSerializer.Feature.Set int features(@NotNull JToml instance) {
        final JTomlOptions options = instance.options();
        @ReflectTomlSerializer.Feature.Set int ret = 0;
        if (options.get(JTomlOption.IGNORE_SERIALIZABLE_MARKER)) ret |= ReflectTomlSerializer.Feature.IGNORE_MARKER;
        if (options.get(JTomlOption.PERMIT_UNSAFE))              ret |= ReflectTomlSerializer.Feature.ALLOW_UNSAFE;
        return ret;
    }

}
