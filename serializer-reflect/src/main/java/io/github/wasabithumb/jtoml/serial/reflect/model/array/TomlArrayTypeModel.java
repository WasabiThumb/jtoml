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

package io.github.wasabithumb.jtoml.serial.reflect.model.array;

import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

@ApiStatus.Internal
final class TomlArrayTypeModel implements ArrayTypeModel<TomlArray> {

    static final TomlArrayTypeModel INSTANCE = new TomlArrayTypeModel();

    //

    @Override
    public @NotNull Class<TomlArray> type() {
        return TomlArray.class;
    }

    @Override
    public @NotNull ParameterizedClass<?> componentType() {
        return new ParameterizedClass<>(TomlValue.class);
    }

    @Override
    public @NotNull TomlArray createNew(int length) {
        return TomlArray.create(length);
    }

    @Override
    public int size(@NotNull TomlArray instance) {
        return instance.size();
    }

    @Override
    public @UnknownNullability Object get(@NotNull TomlArray instance, int index) {
        return instance.get(index);
    }

    @Override
    public void set(@NotNull TomlArray instance, int index, @NotNull Object object) {
        if (index == instance.size()) {
            instance.add((TomlValue) object);
        } else {
            instance.set(index, (TomlValue) object);
        }
    }

}
