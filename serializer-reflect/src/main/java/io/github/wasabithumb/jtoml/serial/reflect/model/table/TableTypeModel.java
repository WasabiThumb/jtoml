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

package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.key.convention.KeyConvention;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.model.TypeModel;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import io.github.wasabithumb.recsup.RecordSupport;
import org.jetbrains.annotations.*;

import java.util.*;

@ApiStatus.Internal
public interface TableTypeModel<T> extends TypeModel<T> {

    @SuppressWarnings("unchecked")
    static <O> @Nullable TableTypeModel<O> match(@NotNull ParameterizedClass<O> pc) {
        Class<O> raw = pc.raw();

        // Record
        if (RecordSupport.isRecord(raw))
            return new RecordTableTypeModel<>(raw);

        // TomlSerializable
        if (TomlSerializable.class.isAssignableFrom(raw))
            return (TableTypeModel<O>) SerializableTableTypeModel.create(raw);

        // TomlTable
        if (TomlTable.class.equals(raw))
            return (TableTypeModel<O>) TomlTableTypeModel.INSTANCE;

        // Map<String, ?>
        ParameterizedClass<?> mt = pc.declaredInterface(Map.class);
        if (mt != null && mt.paramCount() == 2 && String.class.equals(mt.param(0))) {
            ParameterizedClass<?> vt = ParameterizedClass.of(mt.param(1));
            return (TableTypeModel<O>) StringMapTableTypeModel.create(raw.asSubclass(Map.class), vt);
        }

        // Other
        return null;
    }

    //

    @NotNull Builder<T> create();

    @NotNull Mapper mapper(@NotNull KeyConvention defaultConvention);

    /**
     * @apiNote This is often an expensive operation.
     * The intent is to call this method ONCE to build a map.
     */
    @NotNull @Unmodifiable Collection<? extends Key> keys(@NotNull T instance, @NotNull KeyConvention defaultConvention);

    @NotNull ParameterizedClass<?> elementType(@NotNull Key key);

    @UnknownNullability Object get(@NotNull T instance, @NotNull Key key);

    default void applyTableComments(@NotNull Comments comments) { }

    @Contract(mutates = "param2")
    default void applyFieldComments(@NotNull Key key, @NotNull Comments comments) { }

    //

    interface Builder<O> {

        void set(@NotNull Key key, @NotNull Object value);

        @NotNull O build();

    }

    /**
     * Exists to allow the original representation
     * of the key to be retained by implementers
     */
    interface Key {

        @NotNull TomlKey asTomlKey();

        default boolean matches(@NotNull TomlKey other) {
            return this.asTomlKey().equals(other);
        }

    }

    interface Mapper {

        @Nullable Key fromTomlKey(@NotNull TomlKey key);

        default @Nullable Map<TomlKey, Key> universe() {
            return null;
        }

    }

}
