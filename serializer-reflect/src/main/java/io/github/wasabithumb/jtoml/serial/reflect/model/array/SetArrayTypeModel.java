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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

@ApiStatus.Internal
final class SetArrayTypeModel<T extends Set<E>, E> extends CollectionArrayTypeModel<T, E> {

    @SuppressWarnings("unchecked")
    static <IT extends Set<IE>, IE> SetArrayTypeModel<IT, IE> create(
            @NotNull Class<IT> setType,
            @NotNull ParameterizedClass<?> elementType
    ) {
        return new SetArrayTypeModel<>(setType, (ParameterizedClass<IE>) elementType);
    }

    private static int hc(int length) {
        return length * 4 / 3 + 1;
    }

    //

    private SetArrayTypeModel(@NotNull Class<T> type, @NotNull ParameterizedClass<E> elementType) {
        super(type, elementType);
    }

    //

    @Override
    public @NotNull T createNew(int length) {
        if (this.elementType.raw().isEnum() && this.type.isAssignableFrom(EnumSet.class))
            return this.type.cast(EnumSet.noneOf(this.elementType.raw().asSubclass(Enum.class)));

        if (this.type.isAssignableFrom(LinkedHashSet.class))
            return this.type.cast(new LinkedHashSet<>(hc(length)));

        if (Comparable.class.isAssignableFrom(this.elementType.raw()) && this.type.isAssignableFrom(TreeSet.class))
            return this.type.cast(new TreeSet<>());

        return autoConstruct(this.type, hc(length));
    }

}
