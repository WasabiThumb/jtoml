package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.Temporal;

@ApiStatus.Internal
abstract class AbstractTemporalTomlPrimitive<T extends Serializable & Temporal>
        extends AbstractTomlPrimitive<T>
{

    private static void writeDigit(@NotNull StringBuilder sb, @Range(from=0, to=9) int d) {
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

    protected static void writeTime(
            @NotNull StringBuilder sb,
            @NotNull LocalTime time,
            @Range(from = 1, to = 9) int minNanos
    ) {
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
            while (end > minNanos && buf[end - 1] == '0') {
                end--;
            }

            sb.append('.');

            for (int i=0; i < end; i++) {
                sb.append(buf[i]);
            }
        }
    }

    //

    /** @implNote Modified by UnsafePrimitives */
    @Range(from = 1, to = 9) int minNanoResolution;

    protected AbstractTemporalTomlPrimitive(@NotNull Comments comments) {
        super(comments);
        this.minNanoResolution = 1;
    }

}
