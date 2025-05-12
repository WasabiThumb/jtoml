package io.github.wasabithumb.jtoml.configurate;

import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.prop.LineSeparator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

class TomlConfigurationLoaderTest {

    @Test
    void simple(final @TempDir Path tempDir) throws IOException {
        final Path configPath = tempDir.resolve("simple.toml");

        final TomlConfigurationLoader loader = TomlConfigurationLoader.builder()
                .path(configPath)
                .set(JTomlOption.LINE_SEPARATOR, LineSeparator.LF)
                .set(JTomlOption.WRITE_EMPTY_TABLES, true)
                .build();

        final ConfigurationNode node = loader.createNode();
        node.set(new TestConfig1());
        loader.save(node);

        System.out.println("=== START SAVE 1 ===");
        System.out.println(String.join("\n", Files.readAllLines(configPath)));
        System.out.println("=== END SAVE 1 ===");

        final ConfigurationNode loadedNode = loader.load();
        loader.save(loadedNode);

        System.out.println("=== START SAVE 2 ===");
        System.out.println(String.join("\n", Files.readAllLines(configPath)));
        System.out.println("=== END SAVE 2 ===");
    }

    @ConfigSerializable
    public static final class TestConfig1 {
        public String name = "test";
        public int age = 0;
        public boolean isTest = false;
        public String[] tags = new String[0];
        public NestedConfig nested = new NestedConfig();
        public Map<String, NestedConfig> nestedMap = defaultNestedMap();

        private static Map<String, NestedConfig> defaultNestedMap() {
            Map<String, NestedConfig> map = new LinkedHashMap<>();
            map.put("uno", new NestedConfig());
            map.put("dos", new NestedConfig());
            map.put("tres", new NestedConfig());
            return map;
        }

        @ConfigSerializable
        public static final class NestedConfig {
            public String name = "nested";
            public int age = 0;
            public boolean isTest = false;
            public String[] tags = new String[0];
        }
    }
}
