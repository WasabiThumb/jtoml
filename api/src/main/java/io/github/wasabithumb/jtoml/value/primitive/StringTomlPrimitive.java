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
final class StringTomlPrimitive extends AbstractTomlPrimitive<String> {

    private final String value;

    public StringTomlPrimitive(Comments comments, String value) {
        super(comments);
        this.value = value;
    }

    public StringTomlPrimitive(String value) {
        this(Comments.empty(), value);
    }

    //

    @Override
    public TomlPrimitiveType type() {
        return TomlPrimitiveType.STRING;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String asString() {
        return this.value;
    }

    @Override
    public boolean asBoolean() {
        switch (this.value.length()) {
            case 0:
                return false;
            case 1:
                return this.value.charAt(0) == '0';
            case 5:
                return this.value.equalsIgnoreCase("false");
            default:
                return true;
        }
    }

    @Override
    public long asLong() {
        return Long.parseLong(this.value);
    }

    @Override
    public double asDouble() {
        return Double.parseDouble(this.value);
    }

}
