/*
 * Copyright 2026 Xavier Pedraza
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
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.reflect.Defaulting;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jspecify.annotations.NullMarked;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@NullMarked
public final class ToEmptyTestRoute implements TestRoute {

    @Override
    public String displayName() {
        return "Defaulting to Empty";
    }

    @Override
    public void execute(JToml instance) {
        Document doc = instance.fromToml(Document.class, TomlTable.create());
        System.out.println(doc);

        assertTrue(doc.list.isEmpty());
        assertEquals(0, doc.dict.foo);
        assertEquals(1, doc.dict.bar);
        assertEquals(2, doc.dict.baz);
    }

    //

    private record Document(
        @Defaulting.ToEmpty List<String> list,
        @Defaulting.ToEmpty Dict dict
    ) {

        private record Dict(
                @Defaulting.ToInt(0) int foo,
                @Defaulting.ToInt(1) int bar,
                @Defaulting.ToInt(2) int baz
        ) { }

    }

}
