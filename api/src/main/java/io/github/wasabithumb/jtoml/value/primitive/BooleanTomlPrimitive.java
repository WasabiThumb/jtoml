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

package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
final class BooleanTomlPrimitive extends AbstractTomlPrimitive<Boolean> {

    private final boolean value;

    public BooleanTomlPrimitive(Comments comments, boolean value) {
        super(comments);
        this.value = value;
    }

    public BooleanTomlPrimitive(boolean value) {
        this(Comments.empty(), value);
    }

    //

    @Override
    public TomlPrimitiveType type() {
        return TomlPrimitiveType.BOOLEAN;
    }

    @Override
    public Boolean value() {
        return this.value;
    }

    @Override
    public String asString() {
        return Boolean.toString(this.value);
    }

    @Override
    public boolean asBoolean() {
        return this.value;
    }

    @Override
    public long asLong() {
        return this.value ? 1L : 0L;
    }

    @Override
    public double asDouble() {
        return this.value ? 1d : 0d;
    }

}
