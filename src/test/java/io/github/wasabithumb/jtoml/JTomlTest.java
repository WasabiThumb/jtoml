package io.github.wasabithumb.jtoml;

import org.junit.jupiter.api.Test;

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

    // TODO: more tests

}
