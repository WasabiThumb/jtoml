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
