package io.github.wasabithumb.jtoml;

import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.option.prop.PaddingPolicy;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JTomlTest {

    private static final String EXAMPLE =
            "# This is a TOML document. Boom.\n" +
            "\n" +
            "title = \"TOML Example\"\n" +
            "\n" +
            "[owner]\n" +
            "name = \"Lance Uppercut\"\n" +
            "dob = 1979-05-27T07:32:00-08:00 # First class dates? Why not?\n" +
            "\n" +
            "[database]\n" +
            "server = \"192.168.1.1\"\n" +
            "ports = [ 8001, 8001, 8002 ]\n" +
            "connection_max = 5000\n" +
            "enabled = true\n" +
            "\n" +
            "[servers]\n" +
            "\n" +
            "  # You can indent as you please. Tabs or spaces. TOML don't care.\n" +
            "  [servers.alpha]\n" +
            "  ip = \"10.0.0.1\"\n" +
            "  dc = \"eqdc10\"\n" +
            "\n" +
            "  [servers.beta]\n" +
            "  ip = \"10.0.0.2\"\n" +
            "  dc = \"eqdc10\"\n" +
            "\n" +
            "[clients]\n" +
            "data = [ [\"gamma\", \"delta\"], [1, 2] ]\n" +
            "\n" +
            "# Line breaks are OK when inside arrays\n" +
            "hosts = [\n" +
            "  \"alpha\",\n" +
            "  \"omega\"\n" +
            "]";

    @Test
    void example() {
        JToml toml = JToml.jToml();
        toml.readFromString(EXAMPLE);
    }

    @Test
    void writeArrayOfTables() {
        final int childCount = 4;

        TomlArray arr = TomlArray.create();
        for (int i=0; i < childCount; i++) {
            TomlTable child = TomlTable.create();
            child.put("foo", "bar");
            child.put("n", i);
            arr.add(child);
        }

        TomlTable table = TomlTable.create();
        table.put("nodes", arr);

        JTomlOptions options = JTomlOptions.builder()
                .set(JTomlOption.PADDING, PaddingPolicy.NONE)
                .build();
        JToml toml = JToml.jToml(options);
        String str = toml.writeToString(table);

        // Count instances of [[nodes]]
        int count = 0;
        int head = 0;

        while (head < str.length()) {
            head = str.indexOf("[[nodes]]", head);
            if (head == -1) break;
            count++;
            head += 9;
        }

        assertEquals(childCount, count);
    }

    // TODO: more tests

}
