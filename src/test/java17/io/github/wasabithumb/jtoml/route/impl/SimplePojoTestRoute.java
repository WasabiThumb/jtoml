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

package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.key.convention.StandardKeyConvention;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;

import java.time.OffsetDateTime;

import static io.github.wasabithumb.jtoml.route.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SimplePojoTestRoute implements TestRoute {

    @Sentinel("simple.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Simple to POJO";
    }

    @Override
    public void execute(JToml instance) {
        final Document d = this.document;
        assertEquals(MEANING_OF_LIFE, d.meaningOfLife);
        assertEquals(PI,              d.pi           );
        assertEquals(LOREM_IPSUM,          d.loremIpsum   );
        assertEquals(GREAT_MOMENT,    d.greatMoment  );
    }

    //

    @Convention(StandardKeyConvention.KEBAB)
    private static final class Document implements TomlSerializable {
        public int meaningOfLife;
        public double pi;
        public String loremIpsum;
        public OffsetDateTime greatMoment;
    }

}
