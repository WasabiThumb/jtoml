package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.key.convention.KeyConvention;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;
import io.github.wasabithumb.jtoml.serial.reflect.Key;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TrickyConventionTestRoute implements TestRoute.Configuring {

    @Sentinel("tricky-convention.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Tricky Conventions";
    }

    @Override
    public void execute(JToml instance) {
        final Document d = this.document;
        assertEquals(1, d.literalInt);
        assertEquals(2, d.lowerInt);
        assertEquals(3, d.snakeInt);
        assertEquals(4, d.kebabInt);
        assertEquals(5, d.splitInt);
        assertEquals(6, d.custom);
    }

    @Override
    public void configure(JTomlOptions.Builder options) {
        options.set(JTomlOption.DEFAULT_KEY_CONVENTION, FailingKeyConvention.INSTANCE);
    }

    //

    @Convention.Snake
    private record Document(
            @Convention.Literal int literalInt,
            @Convention.Lower int lowerInt,
            int snakeInt,
            @Convention.Kebab int kebabInt,
            @Convention.Split int splitInt,
            @Key("custom") int custom
    ) { }

    /** Used to ensure that all the explicitly defined conventions are actually used */
    private static final class FailingKeyConvention implements KeyConvention {

        static final FailingKeyConvention INSTANCE = new FailingKeyConvention();

        @Override
        public TomlKey toToml(String key) {
            throw new IllegalStateException("Incorrect convention used to resolve key: " + key);
        }

    }

}
