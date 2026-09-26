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
final class OffsetDateTimeTomlPrimitive extends AbstractTemporalTomlPrimitive<OffsetDateTime> {

    private final OffsetDateTime value;

    public OffsetDateTimeTomlPrimitive(
            Comments comments,
            OffsetDateTime value
    ) {
        super(comments);
        TomlValueException.checkDate(value);
        this.value = value;
    }

    public OffsetDateTimeTomlPrimitive(
            OffsetDateTime value
    ) {
        this(Comments.empty(), value);
    }

    //

    @Override
    public TomlPrimitiveType type() {
        return TomlPrimitiveType.OFFSET_DATE_TIME;
    }

    @Override
    public OffsetDateTime value() {
        return this.value;
    }

    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();

        writeDate(sb, this.value.toLocalDate());
        sb.append('T');
        writeTime(sb, this.value.toLocalTime(), this.minNanoResolution);

        int sec = this.value.getOffset().getTotalSeconds();
        if (sec == 0) {
            sb.append('Z');
            return sb.toString();
        } else if (sec < 0) {
            sec = -sec;
            sb.append('-');
        } else {
            sb.append('+');
        }

        int hour = sec / 3600;
        int minute = (sec % 3600) / 60;

        writeHourMinute(sb, hour, minute);
        return sb.toString();
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public long asLong() {
        return this.value.toInstant().toEpochMilli();
    }

    @Override
    public double asDouble() {
        return (double) this.asLong();
    }

    @Override
    public OffsetDateTime asOffsetDateTime() {
        return this.value;
    }

    @Override
    public LocalDateTime asLocalDateTime() {
        return this.value.toLocalDateTime();
    }

    @Override
    public LocalDate asLocalDate() {
        return this.asLocalDateTime().toLocalDate();
    }

    @Override
    public LocalTime asLocalTime() {
        return this.asLocalDateTime().toLocalTime();
    }

}
