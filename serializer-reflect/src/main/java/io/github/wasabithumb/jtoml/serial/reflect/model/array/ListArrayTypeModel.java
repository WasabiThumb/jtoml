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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ApiStatus.Internal
final class ListArrayTypeModel<T extends List<E>, E> extends CollectionArrayTypeModel<T, E> {

    @SuppressWarnings("unchecked")
    static <IT extends List<IE>, IE> ListArrayTypeModel<IT, IE> create(
            @NotNull Class<IT> listType,
            @NotNull ParameterizedClass<?> elementType
    ) {
        return new ListArrayTypeModel<>(listType, (ParameterizedClass<IE>) elementType);
    }

    //

    private ListArrayTypeModel(@NotNull Class<T> type, @NotNull ParameterizedClass<E> elementType) {
        super(type, elementType);
    }

    //

    @Override
    public @NotNull T createNew(int length) {
        if (this.type.isAssignableFrom(ArrayList.class))
            return this.type.cast(new ArrayList<E>(length));

        if (this.type.isAssignableFrom(LinkedList.class))
            return this.type.cast(new LinkedList<E>());

        return autoConstruct(this.type, length);
    }

}
