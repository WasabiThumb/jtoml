package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Ensures the deep entry set works as intended */
public final class DeepEntrySetTestRoute implements TestRoute {

    @Sentinel("holidays.toml")
    private TomlTable document;

    @Override
    public String displayName() {
        return "Deep Entry Set";
    }

    @Override
    public void execute(JToml instance) {
        // Copy the document before mutating, just in case test routes are ever ran multiple times
        TomlTable table = TomlTable.copyOf(this.document);
        assertEquals(10, table.size());

        // Remove some entries
        Set<TomlTable.Entry<?>> entries = table.entries();
        entries.removeIf((TomlTable.Entry<?> entry) ->
                entry.key().toString().contains("J"));

        // Check
        for (TomlTable.Entry<?> entry : entries) {
            assertFalse(entry.key().toString().contains("J"));
        }

        // Remove some more entries
        entries.removeIf((TomlTable.Entry<?> entry) ->
                entry.key().toString().contains("O"));

        // Check
        for (TomlTable.Entry<?> entry : entries) {
            assertFalse(entry.key().toString().contains("O"));
        }

        // Ensure holidays is what we expect
        TomlValue v = table.get("holidays");
        assertNotNull(v);
        assertTrue(v.isTable());
        TomlTable holidays = v.asTable();
        assertEquals(4, holidays.size());
    }
}
