package io.github.wasabithumb.jtoml.route;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class TestConstants {

    public static final int MEANING_OF_LIFE = 42;

    public static final double PI = 3.141592653589793d;

    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    public static final OffsetDateTime GREAT_MOMENT = OffsetDateTime.of(
            LocalDateTime.of(2025, Month.MAY, 7, 18, 44, 5),
            ZoneOffset.ofHoursMinutes(-4, 0)
    );

    //

    private TestConstants() { }

}
