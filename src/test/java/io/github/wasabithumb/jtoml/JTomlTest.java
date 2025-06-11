package io.github.wasabithumb.jtoml;

import io.github.wasabithumb.jtoml.comment.CommentPosition;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlValueException;
import io.github.wasabithumb.jtoml.except.parse.TomlParseException;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.pojo.SimpleTable;
import io.github.wasabithumb.jtoml.test.TestSpec;
import io.github.wasabithumb.jtoml.test.TestSpecs;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class JTomlTest {

    private static JToml TOML;

    @BeforeAll
    static void setup() {
        TOML = JToml.jToml();
    }

    //

    @Test
    void readerWriter() {
        final String basic = "[a]\n# sample comment\nb.c = 'd' # sample inline comment";

        TomlDocument document = assertDoesNotThrow(() -> {
            try (StringReader sr = new StringReader(basic)) {
                return TOML.read(sr);
            }
        });

        TomlValue abc = document.get("a.b.c");
        assertNotNull(abc);
        assertEquals(1, abc.comments().get(CommentPosition.PRE).size());
        assertEquals("sample inline comment", abc.comments().getInline());
        assertTrue(abc.isPrimitive());
        assertEquals("d", abc.asPrimitive().asString());

        assertDoesNotThrow(() -> {
            try (StringWriter sw = new StringWriter()) {
                TOML.write(sw, document);
            }
        });
    }

    @Test
    void reflect() {
        SimpleTable original = SimpleTable.create();
        TomlTable toml = TOML.deserialize(SimpleTable.class, original);
        System.out.println(TOML.writeToString(toml));
        SimpleTable out = TOML.serialize(SimpleTable.class, toml);
        assertEquals(original, out);
    }

    @Test
    void badDateTime() {
        SimpleTable table = SimpleTable.create();
        table.localDate = LocalDate.of(-1, 7, 16); // year -1
        assertThrows(TomlValueException.class, () -> TOML.deserialize(SimpleTable.class, table));
    }

    //

    @TestFactory
    Stream<DynamicTest> read() {
        TestSpecs specs = assertDoesNotThrow(TestSpecs::load, "Failed to load test specs");
        return specs.stream()
                .map((TestSpec ts) -> DynamicTest.dynamicTest(ts.name(), () -> this.readRun(ts)));
    }

    private void readRun(TestSpec spec) {
        if (spec.shouldFail()) {
            assertThrows(
                    TomlParseException.class,
                    () -> this.parse(spec),
                    "Expected test to throw TomlException"
            );
            return;
        }

        TomlTable table = assertDoesNotThrow(() -> this.parse(spec));
        assertDoesNotThrow(() -> spec.validate(table));
    }

    //

    @TestFactory
    Stream<DynamicTest> write() {
        TestSpecs specs = assertDoesNotThrow(TestSpecs::load, "Failed to load test specs");
        return specs.stream()
                .filter((TestSpec ts) -> !ts.shouldFail())
                .map((TestSpec ts) -> DynamicTest.dynamicTest(ts.name(), () -> this.writeRun(ts)));
    }

    private void writeRun(TestSpec spec) {
        TomlTable table1 = assertDoesNotThrow(() -> this.parse(spec));
        String buf = assertDoesNotThrow(() -> TOML.writeToString(table1));
        System.out.println(buf);
        TomlTable table2 = assertDoesNotThrow(() -> TOML.readFromString(buf));
        this.writeEqualsTable(table1, table2);
    }

    private void writeEqualsTable(TomlTable t1, TomlTable t2) {
        SortedSet<TomlKey> k1 = new TreeSet<>(t1.keys());
        SortedSet<TomlKey> k2 = new TreeSet<>(t2.keys());
        assertEquals(k1.size(), k2.size());

        Iterator<TomlKey> ki1 = k1.iterator();
        Iterator<TomlKey> ki2 = k2.iterator();
        TomlKey next;

        while (ki1.hasNext()) {
            next = ki1.next();
            assertEquals(next, ki2.next());
            this.writeEqualsAny(t1.get(next), t2.get(next));
        }
    }

    private void writeEqualsArray(TomlArray a1, TomlArray a2) {
        int len = a1.size();
        assertEquals(len, a2.size());

        for (int i=0; i < len; i++) {
            writeEqualsAny(a1.get(i), a2.get(i));
        }
    }

    private void writeEqualsPrimitive(TomlPrimitive p1, TomlPrimitive p2) {
        assertEquals(p1.type(), p2.type());
        assertEquals(p1.asString(), p2.asString());
    }

    private void writeEqualsAny(TomlValue v1, TomlValue v2) {
        if (v1 == null) {
            assertNull(v2);
            return;
        } else {
            assertNotNull(v2);
        }

        if (v1.isPrimitive()) {
            assertTrue(v2.isPrimitive());
            writeEqualsPrimitive(v1.asPrimitive(), v2.asPrimitive());
        } else if (v1.isArray()) {
            assertTrue(v2.isArray());
            writeEqualsArray(v1.asArray(), v2.asArray());
        } else if (v1.isTable()) {
            assertTrue(v2.isTable());
            writeEqualsTable(v1.asTable(), v2.asTable());
        }
    }

    //

    private TomlTable parse(TestSpec spec) throws TomlException, IOException {
        try (InputStream in = spec.read()) {
            return TOML.read(in);
        }
    }

}
