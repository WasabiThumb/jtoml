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
final class UnionTypeAdapters extends AbstractTypeAdapters {

    private final TypeAdapters[] sub;

    UnionTypeAdapters(
            @NotNull TypeAdapters @NotNull [] sub
    ) {
        this.sub = sub;
    }

    //

    @Override
    public @Nullable <T> TypeAdapter<T> get(@NotNull Class<T> type) {
        TypeAdapter<T> ret;
        for (TypeAdapters adapters : this.sub) {
            ret = adapters.get(type);
            if (ret != null) return ret;
        }
        return null;
    }

    @Override
    protected boolean canFlatten() {
        for (TypeAdapters adapters : this.sub) {
            if (!(adapters instanceof AbstractTypeAdapters)) return false;
            if (!((AbstractTypeAdapters) adapters).canFlatten()) return false;
        }
        return true;
    }

    @Override
    protected void flatten(@NotNull Consumer<? super TypeAdapter<?>> consumer) {
        for (TypeAdapters adapters : this.sub) {
            if (!(adapters instanceof AbstractTypeAdapters)) continue;
            ((AbstractTypeAdapters) adapters).flatten(consumer);
        }
    }

}
