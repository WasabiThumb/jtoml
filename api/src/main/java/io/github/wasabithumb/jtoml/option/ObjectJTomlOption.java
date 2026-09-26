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

package io.github.wasabithumb.jtoml.option;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
final class ObjectJTomlOption<T> extends AbstractJTomlOption<T> {

    public static <R> ObjectJTomlOption<R> of(
            String name,
            Class<R> valueClass,
            R defaultValue
    ) {
        return new ObjectJTomlOption<>(name, valueClass, defaultValue);
    }

    //

    private final Class<T> valueClass;

    public ObjectJTomlOption(String name, Class<T> valueClass, T defaultValue) {
        super(name, defaultValue);
        this.valueClass = valueClass;
    }

    //

    @Override
    public Class<T> valueClass() {
        return this.valueClass;
    }

}
