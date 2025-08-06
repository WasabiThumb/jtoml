package io.github.wasabithumb.jtoml.dummy;

import io.github.wasabithumb.jtoml.Faker;
import io.github.wasabithumb.jtoml.comment.Comment;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.Key;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class PojoTable implements TomlSerializable {

    public static PojoTable create() {
        return Faker.create(PojoTable.class);
    }

    //

    @Comment.Inline("Some text")
    public String text;

    public long integer;

    public double decimal;

    @Key("local-date")
    public LocalDate localDate;

    @Key("local-time")
    public LocalTime localTime;

    @Key("local-date-time")
    public LocalDateTime localDateTime;

    @Key("offset-date-time")
    public OffsetDateTime offsetDateTime;

    public transient short redHerring;
    
    PojoTable() {
        this.redHerring = (short) ThreadLocalRandom.current().nextInt();
    }

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
        if (!(obj instanceof PojoTable)) return false;
        PojoTable other = (PojoTable) obj;

        return Objects.equals(this.text, other.text) &&
                this.integer == other.integer &&
                this.decimal == other.decimal &&
                Objects.equals(this.localDate, other.localDate) &&
                Objects.equals(this.localTime, other.localTime) &&
                Objects.equals(this.localDateTime, other.localDateTime) &&
                Objects.equals(this.offsetDateTime, other.offsetDateTime);
    }

}
