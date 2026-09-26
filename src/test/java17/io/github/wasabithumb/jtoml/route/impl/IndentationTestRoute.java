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
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jspecify.annotations.NullMarked;

import static org.junit.jupiter.api.Assertions.assertEquals;

@NullMarked
public final class IndentationTestRoute implements TestRoute {

    @Sentinel("indentation.toml")
    private String expected;

    //

    @Override
    public String displayName() {
        return "Table Indentation";
    }

    @Override
    public void execute(JToml instance) {
        TomlTable table = createTable();
        String actual = instance.writeToString(table);
        assertEquals(this.expected, actual);
    }

    //

    private static TomlTable createTable() {
        TomlTable superNested = TomlTable.create();
        superNested.put("y-char", 121);
        TomlTable nested = TomlTable.create();
        nested.put("hello", true);
        TomlTable table = TomlTable.create();
        table.put("main", "yes");
        TomlTable document = TomlTable.create();

        nested.put("super-nested", superNested);
        table.put("nested", nested);
        document.put("main", table);
        return document;
    }
}
