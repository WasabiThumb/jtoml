package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.key.convention.StandardKeyConvention;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static io.github.wasabithumb.jtoml.route.TestConstants.*;

public final class SimpleRecordTestRoute implements TestRoute {

    @Sentinel("simple.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Simple to Record";
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
    private record Document(
            int meaningOfLife,
            double pi,
            String loremIpsum,
            OffsetDateTime greatMoment
    ) { }

}
