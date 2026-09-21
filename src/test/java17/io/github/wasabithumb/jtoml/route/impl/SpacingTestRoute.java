package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.option.prop.*;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
