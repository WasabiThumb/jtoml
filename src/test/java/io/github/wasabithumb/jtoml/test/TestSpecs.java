package io.github.wasabithumb.jtoml.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class TestSpecs extends ArrayList<TestSpec> {

    public static TestSpecs load() throws IOException {
        Set<String> tomlFiles = new HashSet<>();
        Set<String> jsonFiles = new HashSet<>();

        try (InputStream is = TestSpec.class.getResourceAsStream("/tests/files-toml-1.0.0")) {
            if (is == null) throw new IOException("Failed to locate tests index");
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (TestSpec.class.getResource("/tests/" + line) == null) continue;
                    if (line.endsWith(".toml")) {
                        tomlFiles.add(line.substring(0, line.length() - 5));
                    } else if (line.endsWith(".json")) {
                        jsonFiles.add(line.substring(0, line.length() - 5));
                    }
                }
            }
        }

        TestSpecs ret = new TestSpecs(tomlFiles.size());

        for (String name : tomlFiles) {
            if (name.startsWith("valid/")) {
                ret.add(TestSpec.valid(name, jsonFiles.contains(name)));
            } else if (name.startsWith("invalid/")) {
                ret.add(TestSpec.invalid(name));
            }
        }

        return ret;
    }

    //

    private TestSpecs(int initialCapacity) {
        super(initialCapacity);
    }

}
