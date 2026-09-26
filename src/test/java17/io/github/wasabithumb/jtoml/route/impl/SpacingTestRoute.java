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
import io.github.wasabithumb.jtoml.option.prop.*;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jspecify.annotations.NullMarked;

import static org.junit.jupiter.api.Assertions.assertTrue;

@NullMarked
public final class SpacingTestRoute implements TestRoute.Configuring {

    @Sentinel("spacing.toml")
    private String expected;

    //

    @Override
    public String displayName() {
        return "Spacing";
    }

    @Override
    public void execute(JToml instance) {
        TomlTable table = TomlTable.create();
        table.put("a.a", "a");
        table.put("a.b", "b");
        table.put("b.c", "c");
        comment(table, "a", "Pre-header spacing of 3");
        comment(table, "a.a", "Post-header spacing of 7 and pre-statement spacing of 2");
        comment(table, "a.b", "Post-statement spacing of 4 and pre-statement spacing of 2");
        comment(table, "b", "Post-statement spacing of 4, post-block spacing of 1 and pre-header spacing of 3");
        comment(table, "b.c", "Post-header spacing of 7 and pre-statement spacing of 2");

        String text = instance.writeToString(table);
        assertTrue(text.startsWith(this.expected));
    }

    @Override
    public void configure(JTomlOptions.Builder options) {
        options
                .set(JTomlOption.SORTING, SortMethod.LEXICOGRAPHICAL)
                .set(JTomlOption.INDENTATION, IndentationPolicy.NONE)
                .set(JTomlOption.PADDING, PaddingPolicy.NONE)
                .set(JTomlOption.LINE_SEPARATOR, LineSeparator.LF)
                .set(JTomlOption.SPACING, SpacingPolicy.builder()
                        .preHeader(3)
                        .postHeader(7)
                        .preStatement(2)
                        .postStatement(4)
                        .postBlock(1)
                        .build());
    }

    private static void comment(TomlTable table, String key, String comment) {
        TomlValue value = table.get(key);
        if (value == null) throw new IllegalStateException();
        value.comments().addPre(comment);
    }

}
