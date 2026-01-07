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
