package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.option.prop.SpacingPolicy;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import static org.junit.jupiter.api.Assertions.*;

public final class WriteCommentsTestRoute implements TestRoute.Configuring {

    @Sentinel("comments.toml")
    private String expected;

    //

    @Override
    public String displayName() {
        return "Write Comments";
    }

    @Override
    public void execute(JToml instance) {
        TomlTable table = createTable();
        String actual = instance.writeToString(table);
        assertEquals(this.expected, actual);
    }

    @Override
    public void configure(JTomlOptions.Builder options) {
        options.set(JTomlOption.SPACING, SpacingPolicy.NONE);
    }

    //

    private static TomlTable createTable() {
        TomlTable ret = TomlTable.create();
        ret.put("inner", createInnerTable());
        return ret;
    }

    private static TomlTable createInnerTable() {
        TomlTable ret = TomlTable.create();

        Comments comments = ret.comments();
        comments.addPre("PRE 1");
        comments.addPre("PRE 2");
        comments.addInline("INLINE A");

        TomlValue value = TomlPrimitive.of(42);
        Comments valueComments = value.comments();
        valueComments.addPre("PRE 1");
        valueComments.addPre("PRE 2");
        valueComments.addInline("INLINE B");
        valueComments.addPost("POST 1");
        valueComments.addPost("POST 2");
        ret.put("value", value);

        return ret;
    }

}
