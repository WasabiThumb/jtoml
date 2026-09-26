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
import io.github.wasabithumb.jtoml.except.TomlValueException;
import org.jetbrains.annotations.ApiStatus;

import java.time.*;

@ApiStatus.Internal
final class LocalDateTomlPrimitive extends AbstractTemporalTomlPrimitive<LocalDate> {

    private final LocalDate value;
    private final ZoneOffset offset;

    public LocalDateTomlPrimitive(
            Comments comments,
            LocalDate value,
            ZoneOffset offset
    ) {
        super(comments);
        TomlValueException.checkDate(value);
        this.value = value;
        this.offset = offset;
    }

    public LocalDateTomlPrimitive(
            LocalDate value,
            ZoneOffset offset
    ) {
        this(Comments.empty(), value, offset);
    }

    //

    @Override
    public TomlPrimitiveType type() {
        return TomlPrimitiveType.LOCAL_DATE;
    }

    @Override
    public LocalDate value() {
        return this.value;
    }

    @Override
    ZoneOffset temporalOffset() {
        return this.offset;
    }

    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();
        writeDate(sb, this.value);
        return sb.toString();
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public long asLong() {
        return this.value.atTime(0, 0).toInstant(this.offset).toEpochMilli();
    }

    @Override
    public double asDouble() {
        return (double) this.asLong();
    }

    @Override
    public OffsetDateTime asOffsetDateTime() {
        return this.asLocalDateTime().atOffset(this.offset);
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        return this.value.atTime(0, 0);
    }

    @Override
    public LocalDate asLocalDate() {
        return this.value;
    }

    @Override
    public LocalTime asLocalTime() {
        return LocalTime.of(0, 0);
    }

}
