package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.key.convention.StandardKeyConvention;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;

import java.time.OffsetDateTime;

import static io.github.wasabithumb.jtoml.route.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SimplePojoTestRoute implements TestRoute {

    @Sentinel("simple.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Simple to POJO";
    }

    @Override
    public void execute(JToml instance) {
        final Document d = this.document;
        assertEquals(MEANING_OF_LIFE, d.meaningOfLife);
        assertEquals(PI,              d.pi           );
        assertEquals(LOREM_IPSUM,          d.loremIpsum   );
        assertEquals(GREAT_MOMENT,    d.greatMoment  );
    }

    //

    @Convention(StandardKeyConvention.KEBAB)
    private static final class Document implements TomlSerializable {
        public int meaningOfLife;
        public double pi;
        public String loremIpsum;
        public OffsetDateTime greatMoment;
    }

}
