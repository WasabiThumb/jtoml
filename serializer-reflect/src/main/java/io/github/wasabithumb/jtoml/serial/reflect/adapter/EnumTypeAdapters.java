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

import java.util.function.Consumer;

@ApiStatus.Internal
final class EnumTypeAdapters extends AbstractTypeAdapters {

    static final EnumTypeAdapters INSTANCE = new EnumTypeAdapters();

    private static @NotNull TypeAdapter<?> unsafe(@NotNull Class<?> type) {
        return new EnumTypeAdapter<>(type.asSubclass(Enum.class));
    }

    //

    private EnumTypeAdapters() { }

    //

    @Override
    public @Nullable <T> TypeAdapter<T> get(@NotNull Class<T> type) {
        if (!type.isEnum()) return null;
        //noinspection unchecked
        return (TypeAdapter<T>) unsafe(type);
    }

    @Override
    protected boolean canFlatten() {
        return false;
    }

    @Override
    protected void flatten(@NotNull Consumer<? super TypeAdapter<?>> consumer) {
        throw new UnsupportedOperationException();
    }

}
