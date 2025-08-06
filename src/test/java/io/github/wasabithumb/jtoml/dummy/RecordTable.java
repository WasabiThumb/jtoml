package io.github.wasabithumb.jtoml.dummy;

import io.github.wasabithumb.jtoml.Faker;
import io.github.wasabithumb.jtoml.comment.Comment;
import io.github.wasabithumb.jtoml.serial.reflect.Key;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record RecordTable(
        @Comment.Inline("Some text")
        String text,

        long integer,

        double decimal,

        @Key("local-date")
        LocalDate localDate,

        @Key("local-time")
        LocalTime localTime,

        @Key("local-date-time")
        LocalDateTime localDateTime,

        @Key("offset-date-time")
        OffsetDateTime offsetDateTime
) {

    public static RecordTable create() {
        return new RecordTable(
                Faker.createString(),
                Faker.createInteger(),
                Faker.createFloat(),
                Faker.createLocalDate(),
                Faker.createLocalTime(),
                Faker.createLocalDateTime(),
                Faker.createOffsetDateTime()
        );
    }

}
