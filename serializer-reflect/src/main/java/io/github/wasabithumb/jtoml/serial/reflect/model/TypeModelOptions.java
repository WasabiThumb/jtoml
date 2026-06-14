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

package io.github.wasabithumb.jtoml.serial.reflect.model;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Propagates configuration from the ReflectTomlSerializer
 * into TypeModel factories. Currently needed by PojoTableTypeModel
 * and thus propagated through TableTypeModel and TypeModel.
 */
@ApiStatus.Internal
public final class TypeModelOptions {

    private static final int O_IGNORE_MARKER = 1;
    private static final int O_ALLOW_UNSAFE = 2;

    //

    private final int value;

    public TypeModelOptions(
            boolean ignoreMarker,
            boolean allowUnsafe
    ) {
        int value = 0;
        if (ignoreMarker) value |= O_IGNORE_MARKER;
        if (allowUnsafe) value |= O_ALLOW_UNSAFE;
        this.value = value;
    }

    //

    public boolean ignoreMarker() {
        return (this.value & O_IGNORE_MARKER) == O_IGNORE_MARKER;
    }

    public boolean allowUnsafe() {
        return (this.value & O_ALLOW_UNSAFE) == O_ALLOW_UNSAFE;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeModelOptions &&
                this.value == ((TypeModelOptions) obj).value;
    }

    @Override
    public @NotNull String toString() {
        return "TypeModelOptions[" +
                "ignoreMarker=" + this.ignoreMarker() + ", " +
                "allowUnsafe=" + this.allowUnsafe() +
                "]";
    }

}
