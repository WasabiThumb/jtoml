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
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.reflect.Defaulting;

import static io.github.wasabithumb.jtoml.route.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

public final class DefaultingWithValueTestRoute implements TestRoute {

    @Sentinel("defaulting.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Defaulting with Value";
    }

    @Override
    public void execute(JToml instance) {
        assertEquals(0xCAFEB015L, this.document.sanity);
        assertEquals(LOREM_IPSUM, this.document.someString);
        assertTrue(this.document.someBool);
        assertEquals(PI, this.document.someFloat);
        assertEquals(MEANING_OF_LIFE, this.document.someInt);
    }

    //

    private record Document(
            long sanity,
            @Defaulting.ToString(LOREM_IPSUM) String someString,
            @Defaulting.ToBool(true) boolean someBool,
            @Defaulting.ToFloat(PI) double someFloat,
            @Defaulting.ToInt(MEANING_OF_LIFE) int someInt
    ) { }

}
