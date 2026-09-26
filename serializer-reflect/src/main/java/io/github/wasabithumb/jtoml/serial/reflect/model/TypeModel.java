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

package io.github.wasabithumb.jtoml.serial.reflect.model;

import io.github.wasabithumb.jtoml.serial.reflect.model.array.ArrayTypeModel;
import io.github.wasabithumb.jtoml.serial.reflect.model.table.TableTypeModel;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * Describes the component types of a Java type for the
 * purposes of TOML de/serialization. The components
 * are to be checked for recursion and a branch is to
 * be executed to process it.
 */
@ApiStatus.Internal
public interface TypeModel<T> {

    static <O> TypeModel<O> of(
            ParameterizedClass<O> cls,
            TypeModelOptions options
    ) {
        TypeModel<O> candidate;

        // Tables
        candidate = TableTypeModel.match(cls, options);
        if (candidate != null) return candidate;

        // Arrays
        candidate = ArrayTypeModel.match(cls);
        if (candidate != null) return candidate;

        // Everything else
        return new Basic<>(cls.raw());
    }

    //

    Class<T> type();

    default boolean isArray() {
        return this instanceof ArrayTypeModel<?>;
    }

    @Contract("-> this")
    default ArrayTypeModel<T> asArray() throws ClassCastException {
        return (ArrayTypeModel<T>) this;
    }

    default boolean isTable() {
        return this instanceof TableTypeModel<?>;
    }

    @Contract("-> this")
    default TableTypeModel<T> asTable() throws ClassCastException {
        return (TableTypeModel<T>) this;
    }

    //

    final class Basic<T> implements TypeModel<T> {

        private final Class<T> type;

        private Basic(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<T> type() {
            return this.type;
        }

    }

}
