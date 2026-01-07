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
import org.jetbrains.annotations.Range;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

@ApiStatus.Internal
abstract class AbstractTomlPrimitive<T extends Serializable> implements TomlPrimitive {

    protected static void writeDigit(@NotNull StringBuilder sb, @Range(from=0, to=9) int d) {
        sb.append((char) (d + '0'));
    }

    protected static void writeDate(@NotNull StringBuilder sb, @NotNull LocalDate date) {
        int year = date.getYear();
        if (year > 9999) {
            sb.append(year);
        } else {
            writeDigit(sb, year / 1000);
            writeDigit(sb, (year % 1000) / 100);
            writeDigit(sb, (year % 100) / 10);
            writeDigit(sb, year % 10);
        }
        sb.append('-');

        int month = date.getMonthValue();
        writeDigit(sb, month / 10);
        writeDigit(sb, month % 10);
        sb.append('-');

        int day = date.getDayOfMonth();
        writeDigit(sb, day / 10);
        writeDigit(sb, day % 10);
    }

    protected static void writeHourMinute(@NotNull StringBuilder sb, int hour, int minute) {
        writeDigit(sb, hour / 10);
        writeDigit(sb, hour % 10);
        sb.append(':');
        writeDigit(sb, minute / 10);
        writeDigit(sb, minute % 10);
    }

    protected static void writeTime(@NotNull StringBuilder sb, @NotNull LocalTime time) {
        writeHourMinute(sb, time.getHour(), time.getMinute());
        sb.append(':');

        int second = time.getSecond();
        writeDigit(sb, second / 10);
        writeDigit(sb, second % 10);

        int nano = time.getNano();
        if (nano != 0) {
            char[] buf = new char[9];

            for (int i=0; i < 9; i++) {
                buf[8 - i] = (char) ((nano % 10) + '0');
                nano /= 10;
            }

            int end = 9;
            while (end > 3 && buf[end - 1] == '0') {
                end--;
            }

            sb.append('.');

            for (int i=0; i < end; i++) {
                sb.append(buf[i]);
            }
        }
    }

    //

    protected final long creationTime;
    protected final Comments comments;
    protected transient byte flags;

    protected AbstractTomlPrimitive(@NotNull Comments comments) {
        this.creationTime = System.nanoTime();
        this.comments = comments;
        this.flags = (byte) 0;
    }

    //

    @Override
    public long creationTime() {
        return this.creationTime;
    }

    @Override
    public int flags() {
        return this.flags & 0xFF;
    }

    @Override
    public @NotNull TomlPrimitive flags(int flags) {
        this.flags = (byte) flags;
        return this;
    }

    @Override
    public @NotNull Comments comments() {
        return this.comments;
    }

    @Override
    public abstract @NotNull T value();

    @ApiStatus.Internal
    @NotNull ZoneOffset temporalOffset() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Primitive has no temporal offset");
    }

    //

    @Override
    public int hashCode() {
        int h = 7;
        h = 31 * h + this.type().hashCode();
        h = 31 * h + this.value().hashCode();
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TomlPrimitive)) return false;
        TomlPrimitive other = (TomlPrimitive) obj;
        if (this.type() != other.type()) return false;
        return this.value().equals(other.value());
    }

    @Override
    public @NotNull String toString() {
        return "TomlPrimitive[type=" + this.type().name() + ", value=" + this.value() + "]";
    }

}
