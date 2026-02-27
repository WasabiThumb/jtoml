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
