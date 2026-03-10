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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Internal
final class MapTypeAdapters extends AbstractTypeAdapters {

    private final Map<Class<?>, TypeAdapter<?>> map;

    MapTypeAdapters(
            @NotNull Map<Class<?>, TypeAdapter<?>> map
    ) {
        this.map = map;
    }

    //


    @Override
    public @Nullable <T> TypeAdapter<T> get(@NotNull Class<T> type) {
        TypeAdapter<?> ret = this.map.get(type);
        if (ret == null) return null;
        //noinspection unchecked
        return (TypeAdapter<T>) ret;
    }

    @Override
    protected boolean canFlatten() {
        return true;
    }

    @Override
    protected void flatten(@NotNull Consumer<? super TypeAdapter<?>> consumer) {
        for (TypeAdapter<?> value : this.map.values()) consumer.accept(value);
    }

}
