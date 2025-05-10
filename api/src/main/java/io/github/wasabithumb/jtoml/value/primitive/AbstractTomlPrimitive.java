package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

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
        if (nano >= 1000000) {
            int millis = nano / 1000000;
            sb.append('.');
            writeDigit(sb, millis / 100);
            writeDigit(sb, (millis % 100) / 10);
            writeDigit(sb, millis % 10);
        }
    }

    //

    @Override
    public abstract @NotNull T value();

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
