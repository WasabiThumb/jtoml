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

package io.github.wasabithumb.jtoml;

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import io.github.wasabithumb.jtoml.except.parse.TomlParseException;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.route.TestRouteRunner;
import io.github.wasabithumb.jtoml.route.TestRoutes;
import io.github.wasabithumb.jtoml.spec.TestSpec;
import io.github.wasabithumb.jtoml.spec.TestSpecs;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Entry point for JToml tests.
 * This is the only class currently using
 * JUnit injection annotations, the rest
 * of the test source is fixtures that are
 * inevitably used by this class.
 */
@NullMarked
class JTomlTest {

    private static @UnknownNullability JToml TOML;

    @BeforeAll
    static void setup() {
        TOML = JToml.jToml();
    }

    //

    /**
     * Tests that the rather optimized
     * {@link TomlKey} APIs are
     * functioning as intended.
     */
    @Test
    void keys() {
        // Make a key in a very contrived way
        TomlKey key = TomlKey.join(
                TomlKey.literal("a", "b"),
                TomlKey.join(
                        TomlKey.parse("c.'d'"),
                        TomlKey.literal("e", "f")
                ),
                TomlKey.join(
                        TomlKey.literal("g", "h"),
                        TomlKey.parse("\"i\".j.k")
                ),
                TomlKey.literal("l.m.n.o.p"),
                TomlKey.parse("q.r.s")
        ).slice(2, 12);

        // Ensure we got the right key
        assertEquals(
                TomlKey.parse("c.d.e.f.g.h.i.j.k.'l.m.n.o.p'"),
                key
        );

        // Iterate maliciously
        ListIterator<String> iter = key.listIterator(4);

        assertTrue(iter.hasNext());
        assertEquals(4, iter.nextIndex());
        assertEquals("g", iter.next());

        assertTrue(iter.hasPrevious());
        assertEquals(4, iter.previousIndex());
        assertEquals("g", iter.previous());

        assertTrue(iter.hasPrevious());
        assertEquals(3, iter.previousIndex());
        assertEquals("f", iter.previous());

        for (int i = 2; i >= 0; i--) {
            assertTrue(iter.hasPrevious());
            assertEquals(i, iter.previousIndex());
            iter.previous();
        }
        assertFalse(iter.hasPrevious());

        for (int i = 0; i < 9; i++) {
            assertTrue(iter.hasNext());
            assertEquals(i, iter.nextIndex());
            iter.next();
        }

        assertTrue(iter.hasNext());
        assertEquals(9, iter.nextIndex());
        assertEquals("l.m.n.o.p", iter.next());
        assertFalse(iter.hasNext());
    }

    /**
     * Runs every test route in {@code io.github.wasabithumb.jtoml.route.impl}.
     * Test routes are for testing library features, not language features,
     * which should be covered by the official test suite.
     */
    @TestFactory
    Stream<DynamicTest> routes() {
        return TestRoutes.stream()
                .map(TestRouteRunner::newDynamicTest);
    }

    /**
     * Ensures that every valid and invalid test case in the official test
     * suite (fetched by the {@code fetchTests} Gradle task and piped into
     * {@code processTestRecourses}) either succeeds and matches the expected
     * AST exactly or fails in the expected way.
     */
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

    /**
     * Ensures that every valid test case in the official test
     * suite (fetched by the {@code fetchTests} Gradle task and piped into
     * {@code processTestRecourses}) can be parsed, written, re-parsed,
     * and that the parsed and re-parsed documents are semantically
     * identical.
     */
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

    private void writeEqualsAny(@Nullable TomlValue v1, @Nullable TomlValue v2) {
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
        } catch (TomlIOException e) {
            throw e.getCause();
        }
    }

}
