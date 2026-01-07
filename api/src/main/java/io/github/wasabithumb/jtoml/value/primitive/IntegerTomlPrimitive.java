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

package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApiStatus.Internal
final class IntegerTomlPrimitive extends AbstractTomlPrimitive<Long> {

    private final long value;

    public IntegerTomlPrimitive(@NotNull Comments comments, long value) {
        super(comments);
        this.value = value;
    }

    public IntegerTomlPrimitive(long value) {
        this(Comments.empty(), value);
    }

    //

    @Override
    public @NotNull TomlPrimitiveType type() {
        return TomlPrimitiveType.INTEGER;
    }

    @Override
    public @NotNull Long value() {
        return this.value;
    }

    @Override
    public @NotNull String asString() {
        return Long.toString(this.value);
    }

    @Override
    public boolean asBoolean() {
        return this.value != 0L;
    }

    @Override
    public long asLong() {
        return this.value;
    }

    @Override
    public double asDouble() {
        return (double) this.value;
    }

    @Override
    public @NotNull OffsetDateTime asOffsetDateTime() {
        return Instant.ofEpochMilli(this.value).atOffset(ZoneOffset.UTC);
    }

    @Override
    public @NotNull LocalDateTime asLocalDateTime() {
        return LocalDateTime.ofEpochSecond(
                this.value / 1000L,
                ((int) (this.value % 1000)) * 1000000,
                ZoneOffset.UTC
        );
    }

    @Override
    public @NotNull Instant asInstant() {
        return Instant.ofEpochMilli(this.value);
    }

}
