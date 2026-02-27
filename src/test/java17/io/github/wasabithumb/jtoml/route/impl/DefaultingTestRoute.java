package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.reflect.Defaulting;

import static io.github.wasabithumb.jtoml.route.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

public final class DefaultingTestRoute implements TestRoute {

    @Sentinel("defaulting.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Defaulting";
    }

    @Override
    public void execute(JToml instance) {
        assertEquals(0xCAFEB015L, this.document.sanity);
        assertNull(this.document.someString);
        assertFalse(this.document.someBool);
        assertEquals(0d, this.document.someFloat);
        assertEquals(0, this.document.someInt);
    }

    //

    @Defaulting
    private record Document(
            long sanity,
            String someString,
            boolean someBool,
            double someFloat,
            int someInt
    ) { }

}
