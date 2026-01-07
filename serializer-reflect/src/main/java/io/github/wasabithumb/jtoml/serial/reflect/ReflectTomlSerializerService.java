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
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.serial.TomlSerializerService;
import io.github.wasabithumb.jtoml.serial.reflect.adapter.TypeAdapters;
import io.github.wasabithumb.recsup.RecordSupport;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

@ApiStatus.Internal
public final class ReflectTomlSerializerService extends TomlSerializerService {

    @Override
    public boolean canSerializeTo(@NotNull Class<?> outType) {
        if (RecordSupport.isRecord(outType)) return true;
        int mod = outType.getModifiers();
        return TomlSerializable.class.isAssignableFrom(outType) &&
                !Modifier.isInterface(mod) &&
                !Modifier.isAbstract(mod);
    }

    @Override
    public boolean canDeserializeFrom(@NotNull Class<?> inType) {
        if (RecordSupport.isRecord(inType)) return true;
        return TomlSerializable.class.isAssignableFrom(inType);
    }

    //

    @Override
    public @NotNull <T> TomlSerializer<?, T> getSerializer(@NotNull JToml instance, @NotNull Class<T> outType) {
        return new ReflectTomlSerializer<>(
                outType,
                TypeAdapters.standard(),
                ReflectTomlSerializer.C_SERIALIZE
        );
    }

    @Override
    public @NotNull <T> TomlSerializer<T, ?> getDeserializer(@NotNull JToml instance, @NotNull Class<T> inType) {
        return new ReflectTomlSerializer<>(
                inType,
                TypeAdapters.standard(),
                ReflectTomlSerializer.C_DESERIALIZE
        );
    }

}
