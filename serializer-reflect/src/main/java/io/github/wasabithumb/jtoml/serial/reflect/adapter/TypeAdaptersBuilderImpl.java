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

import java.util.*;

@ApiStatus.Internal
final class TypeAdaptersBuilderImpl implements TypeAdapters.Builder {

    private static final int F_HAS_FLATTENED = 1;
    private static final int F_HAS_UNFLATTENED = 2;

    static final TypeAdapters STANDARD = (new TypeAdaptersBuilderImpl())
            .add(TypeAdapter.values())
            .merge(EnumTypeAdapters.INSTANCE)
            .build();

    //

    private final Map<Class<?>, TypeAdapter<?>> flattened = new HashMap<>();
    private final Set<TypeAdapters> unflattened = new LinkedHashSet<>();

    //

    @Override
    public @NotNull TypeAdaptersBuilderImpl clear() {
        this.flattened.clear();
        this.unflattened.clear();
        return this;
    }

    @Override
    public @NotNull TypeAdaptersBuilderImpl merge(@NotNull TypeAdapters adapters) {
        AbstractTypeAdapters qual;
        if (adapters instanceof AbstractTypeAdapters &&
                (qual = (AbstractTypeAdapters) adapters).canFlatten()
        ) {
            qual.flatten(this::add);
        } else {
            this.unflattened.add(adapters);
        }
        return this;
    }

    @Override
    public @NotNull TypeAdaptersBuilderImpl add(@NotNull TypeAdapter<?> adapter) {
        this.flattened.put(adapter.typeClass(), adapter);
        return this;
    }

    @Override
    public @NotNull TypeAdapters build() {
        int f = 0;
        if (!this.flattened.isEmpty()) f |= F_HAS_FLATTENED;
        if (!this.unflattened.isEmpty()) f |= F_HAS_UNFLATTENED;

        switch (f) {
            case 0:
                return EmptyTypeAdapters.INSTANCE;
            case F_HAS_FLATTENED:
                return this.moveFlattened();
            case F_HAS_UNFLATTENED:
                if (this.unflattened.size() == 1) return this.unflattened.iterator().next();
                return new UnionTypeAdapters(this.moveUnflattened(null));
            case F_HAS_FLATTENED | F_HAS_UNFLATTENED:
                return new UnionTypeAdapters(this.moveUnflattened(this.moveFlattened()));
            default:
                throw new AssertionError();
        }
    }

    private @NotNull TypeAdapters moveFlattened() {
        int size = this.flattened.size();
        if (size == 1) return new SingleTypeAdapters(this.flattened.values().iterator().next());
        Map<Class<?>, TypeAdapter<?>> map = new HashMap<>(size * 4 / 3 + 1);
        map.putAll(this.flattened);
        map = Collections.unmodifiableMap(map);
        return new MapTypeAdapters(map);
    }

    private @NotNull TypeAdapters @NotNull [] moveUnflattened(@Nullable TypeAdapters insert) {
        int len = this.unflattened.size();
        TypeAdapters[] ret;
        int i;
        if (insert != null) {
            ret = new TypeAdapters[++len];
            ret[0] = insert;
            i = 1;
        } else {
            ret = new TypeAdapters[len];
            i = 0;
        }

        Iterator<TypeAdapters> iter = this.unflattened.iterator();
        for (; i < len; i++) ret[i] = iter.next();

        return ret;
    }

}
