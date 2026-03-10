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

import io.github.wasabithumb.jtoml.serial.reflect.model.TypeModel;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@ApiStatus.Internal
public interface ArrayTypeModel<T> extends TypeModel<T> {

    @SuppressWarnings("unchecked")
    static <O> @Nullable ArrayTypeModel<O> match(@NotNull ParameterizedClass<O> pc) {
        Class<O> raw = pc.raw();

        // TomlArray
        if (TomlArray.class.equals(raw))
            return (ArrayTypeModel<O>) TomlArrayTypeModel.INSTANCE;

        // Array
        if (raw.isArray())
            return new DirectArrayTypeModel<>(raw, raw.getComponentType());

        // List<?>
        ParameterizedClass<?> list = pc.declaredInterface(List.class);
        if (list != null && list.paramCount() >= 1) {
            ParameterizedClass<?> elementType = ParameterizedClass.of(list.param(0));
            return (ArrayTypeModel<O>) ListArrayTypeModel.create(raw.asSubclass(List.class), elementType);
        }

        // Set<?>
        ParameterizedClass<?> set = pc.declaredInterface(Set.class);
        if (set != null && set.paramCount() >= 1) {
            ParameterizedClass<?> elementType = ParameterizedClass.of(set.param(0));
            return (ArrayTypeModel<O>) SetArrayTypeModel.create(raw.asSubclass(Set.class), elementType);
        }

        // Other
        return null;
    }

    //

    @NotNull ParameterizedClass<?> componentType();

    @NotNull T createNew(int length);

    int size(@NotNull T instance);

    @NotNull Iterator<?> iterator(@NotNull T instance);

    void put(@NotNull T instance, @NotNull Object object);

}
