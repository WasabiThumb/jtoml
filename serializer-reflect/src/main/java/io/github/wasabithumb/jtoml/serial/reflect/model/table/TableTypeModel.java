/*
 * Copyright 2026 Xavier Pedraza
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

package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.key.convention.KeyConvention;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.model.TypeModel;
import io.github.wasabithumb.jtoml.serial.reflect.model.TypeModelOptions;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import io.github.wasabithumb.recsup.RecordSupport;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
public interface TableTypeModel<T> extends TypeModel<T> {

    @SuppressWarnings("unchecked")
    static <O> @Nullable TableTypeModel<O> match(
            ParameterizedClass<O> pc,
            TypeModelOptions options
    ) {
        Class<O> raw = pc.raw();

        // Record
        if (RecordSupport.isRecord(raw))
            return new RecordTableTypeModel<>(raw);

        // POJO (marked with TomlSerializable)
        if (!options.ignoreMarker() && TomlSerializable.class.isAssignableFrom(raw))
            return new PojoTableTypeModel<>(raw, options);

        // TomlTable
        if (TomlTable.class.equals(raw))
            return (TableTypeModel<O>) TomlTableTypeModel.INSTANCE;

        // Map<String, ?>
        ParameterizedClass<?> mt = pc.declaredInterface(Map.class);
        if (mt != null && mt.paramCount() == 2 && String.class.equals(mt.param(0))) {
            ParameterizedClass<?> vt = ParameterizedClass.of(mt.param(1));
            return (TableTypeModel<O>) StringMapTableTypeModel.create(raw.asSubclass(Map.class), vt);
        }

        // POJO (not marked with TomlSerializable)
        if (options.ignoreMarker())
            return new PojoTableTypeModel<>(raw, options);

        // Other
        return null;
    }

    //

    Builder<T> create();

    Mapper mapper(KeyConvention defaultConvention);

    /**
     * @apiNote This is often an expensive operation.
     * The intent is to call this method ONCE to build a map.
     */
    @Unmodifiable Collection<? extends Key> keys(T instance, KeyConvention defaultConvention);

    ParameterizedClass<?> elementType(Key key);

    @UnknownNullability Object get(T instance, Key key);

    default void applyTableComments(Comments comments) { }

    @Contract(mutates = "param2")
    default void applyFieldComments(Key key, Comments comments) { }

    //

    interface Builder<O> {

        void set(Key key, Object value);

        O build();

    }

    interface Key {

        TomlKey asTomlKey();

        default boolean isDefaulting() {
            return false;
        }

        default @Nullable Object defaultValue() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

    }

    interface Mapper {

        @Nullable Key fromTomlKey(TomlKey key);

        default @Nullable Map<TomlKey, Key> universe() {
            return null;
        }

    }

}
