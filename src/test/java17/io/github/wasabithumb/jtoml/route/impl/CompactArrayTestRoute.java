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
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.option.prop.IndentationPolicy;
import io.github.wasabithumb.jtoml.option.prop.SpacingPolicy;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import static org.junit.jupiter.api.Assertions.*;

/** For debugging/covering #86 */
public final class CompactArrayTestRoute implements TestRoute.Configuring {

    @Override
    public String displayName() {
        return "Compact Array";
    }

    @Override
    public void execute(JToml instance) {
        // Create a nested empty array: [[]]
        TomlArray array = TomlArray.create();
        array.add(TomlArray.create());

        // Insert into a table
        TomlTable table = TomlTable.create();
        table.put("-845656543", array);

        // Serialize
        String doc = instance.writeToString(table);

        // Deserialize and check
        TomlTable out = instance.readFromString(doc);
        TomlValue inner = out.get("-845656543");
        assertNotNull(inner);
        assertTrue(inner.isArray());
        assertEquals(1, inner.asArray().size());
        assertTrue(inner.asArray().get(0).isArray());
        assertEquals(0, inner.asArray().get(0).asArray().size());
    }

    @Override
    public void configure(JTomlOptions.Builder options) {
        options
                .set(JTomlOption.SPACING, SpacingPolicy.NONE)
                .set(JTomlOption.INDENTATION, IndentationPolicy.NONE);
    }

}
