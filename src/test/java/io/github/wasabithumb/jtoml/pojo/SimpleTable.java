package io.github.wasabithumb.jtoml.pojo;

import io.github.wasabithumb.jtoml.serial.TomlSerializable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class SimpleTable implements TomlSerializable {

    public static SimpleTable create() {
        SimpleTable ret = new SimpleTable();
        ret.text = "hello\njtoml ❤";
        ret.integer = 1234;
        ret.decimal = 56.789;
        ret.localDate = LocalDate.of(1996, 1, 23);
        ret.localTime = LocalTime.of(16, 50);
        ret.localDateTime = LocalDateTime.now();
        ret.offsetDateTime = OffsetDateTime.now();
        return ret;
    }

    //

    public String text;
    public long integer;
    public double decimal;
    public LocalDate localDate;
    public LocalTime localTime;
    public LocalDateTime localDateTime;
    public OffsetDateTime offsetDateTime;
    
    SimpleTable() { }

    //


    @Override
    public int hashCode() {
        return Objects.hash(
                this.text,
                this.integer,
                this.decimal,
                this.localDate,
                this.localTime,
                this.localDateTime,
                this.offsetDateTime
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleTable)) return false;
        SimpleTable other = (SimpleTable) obj;

        return Objects.equals(this.text, other.text) &&
                this.integer == other.integer &&
                this.decimal == other.decimal &&
                Objects.equals(this.localDate, other.localDate) &&
                Objects.equals(this.localTime, other.localTime) &&
                Objects.equals(this.localDateTime, other.localDateTime) &&
                Objects.equals(this.offsetDateTime, other.offsetDateTime);
    }

}
