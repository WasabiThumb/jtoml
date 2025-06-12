package io.github.wasabithumb.jtoml.dummy;

import io.github.wasabithumb.jtoml.Faker;
import io.github.wasabithumb.jtoml.comment.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record RecordTable(
        @Comment.Inline("Some text")
        String text,
        long integer,
        double decimal,
        LocalDate localDate,
        LocalTime localTime,
        LocalDateTime localDateTime,
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
