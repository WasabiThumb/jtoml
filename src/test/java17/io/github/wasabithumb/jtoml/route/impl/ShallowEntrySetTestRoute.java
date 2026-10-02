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

package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Ensures the shallow entry set works as intended */
public final class ShallowEntrySetTestRoute implements TestRoute {

    @Sentinel("holidays.toml")
    private TomlTable document;

    @Override
    public String displayName() {
        return "Shallow Entry Set";
    }

    @Override
    public void execute(JToml instance) {
        // Copy the document before mutating, just in case test routes are ever ran multiple times
        TomlTable table = TomlTable.copyOf(this.document);
        assertEquals(10, table.size());

        // Make sure the shallow entry set is what we expect
        Set<TomlTable.Entry<?>> entries = table.entries(false);
        assertEquals(2, entries.size());
        Iterator<TomlTable.Entry<?>> iter = entries.iterator();
        assertTrue(iter.hasNext());
        assertEquals(TomlKey.literal("holidays"), iter.next().key());
        assertTrue(iter.hasNext());
        assertEquals(TomlKey.literal("months"), iter.next().key());

        // Remove the "months" array through the iterator
        assertTrue(entries.removeIf((TomlTable.Entry<?> entry) -> TomlKey.literal("months").equals(entry.key())));
        assertEquals(1, entries.size());
        assertEquals(9, table.size());
    }
}
