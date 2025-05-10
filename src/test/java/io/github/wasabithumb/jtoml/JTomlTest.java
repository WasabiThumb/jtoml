package io.github.wasabithumb.jtoml;

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.test.TestSpec;
import io.github.wasabithumb.jtoml.test.TestSpecs;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class JTomlTest {

    private static JToml TOML;

    @BeforeAll
    static void setup() {
        TOML = JToml.jToml();
    }

    @TestFactory
    Stream<DynamicTest> suite() {
        TestSpecs specs = assertDoesNotThrow(TestSpecs::load, "Failed to load test specs");
        return specs.stream()
                .map((TestSpec ts) -> DynamicTest.dynamicTest(ts.name(), () -> this.suiteRun(ts)));
    }

    private void suiteRun(TestSpec spec) {
        if (spec.shouldFail()) {
            assertThrows(
                    TomlException.class,
                    () -> this.suiteRead(spec),
                    "Expected test to throw TomlException"
            );
            return;
        }

        TomlTable table = assertDoesNotThrow(() -> this.suiteRead(spec), "Failed to read");
        assertDoesNotThrow(() -> spec.validate(table), "Failed to validate");
    }

    private TomlTable suiteRead(TestSpec spec) throws TomlException, IOException {
        try (InputStream in = spec.read()) {
            return TOML.read(in);
        }
    }

    // TODO: more tests

}
