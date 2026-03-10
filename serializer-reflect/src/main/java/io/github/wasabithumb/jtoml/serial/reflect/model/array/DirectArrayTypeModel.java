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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.stream.IntStream;

@ApiStatus.Internal
final class DirectArrayTypeModel<T> implements ArrayTypeModel<T> {

    private final Class<T> arrayType;
    private final Class<?> elementType;
    private int head = 0;

    DirectArrayTypeModel(@NotNull Class<T> arrayType, @NotNull Class<?> elementType) {
        this.arrayType = arrayType;
        this.elementType = elementType;
    }

    //

    @Override
    public @NotNull Class<T> type() {
        return this.arrayType;
    }

    @Override
    public @NotNull ParameterizedClass<?> componentType() {
        return new ParameterizedClass<>(this.elementType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull T createNew(int length) {
        return (T) Array.newInstance(this.elementType, length);
    }

    @Override
    public int size(@NotNull T instance) {
        return Array.getLength(instance);
    }

    @Override
    public @NotNull Iterator<?> iterator(@NotNull T instance) {
        return IntStream.range(0, Array.getLength(instance))
                .mapToObj((int i) -> Array.get(instance, i))
                .iterator();
    }

    @Override
    public void put(@NotNull T instance, @NotNull Object object) {
        Array.set(instance, this.head++, this.elementType.cast(object));
    }

}