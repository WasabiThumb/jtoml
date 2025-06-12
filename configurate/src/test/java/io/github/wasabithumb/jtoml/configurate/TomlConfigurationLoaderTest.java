package io.github.wasabithumb.jtoml.configurate;

import io.github.wasabithumb.jtoml.Faker;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.prop.LineSeparator;
import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic sanity checks for the loader.
 */
class TomlConfigurationLoaderTest {

    @Test
    void testSimpleLoading() throws ConfigurateException {
        final URL url = this.getClass().getResource("/example.toml");
        final ConfigurationLoader<BasicConfigurationNode> loader = TomlConfigurationLoader.builder()
                .url(url).build();
        final ConfigurationNode node = loader.load();
        assertEquals("unicorn", node.node("test", "op-level").raw());
        assertEquals("dragon", node.node("other", "op-level").raw());
        assertEquals("dog park", node.node("other", "location").raw());


        final List<Map<String, List<String>>> fooList = new ArrayList<>(node.node("foo")
                .getList(new TypeToken<Map<String, List<String>>>() {}));
        assertEquals(0, fooList.get(0).get("bar").size());
    }

    @Test
    void testReadWithTabs() throws ConfigurateException {
        final ConfigurationNode expected = BasicConfigurationNode.root(n -> {
            n.node("document").act(d -> {
                d.node("we").raw("support tabs");
                d.node("and").raw("literal tabs\tin strings");
                d.node("with").act(w -> {
                    w.appendListNode().raw("more levels");
                    w.appendListNode().raw("of indentation");
                });
            });
        });

        final URL url = this.getClass().getResource("/tab-example.toml");
        final ConfigurationLoader<BasicConfigurationNode> loader = TomlConfigurationLoader.builder()
                .url(url).build();
        final ConfigurationNode node = loader.load();
        assertEquals(expected, node);
    }

    @Test
    void testWriteBasicFile(final @TempDir Path tempDir) throws ConfigurateException, IOException {
        final Path target = tempDir.resolve("write-basic.toml");
        final ConfigurationNode node = BasicConfigurationNode.root(n -> {
            n.node("mapping", "first").set("hello");
            n.node("mapping", "second").set("world");

            n.node("list").act(c -> {
                c.appendListNode().set(1);
                c.appendListNode().set(2);
                c.appendListNode().set(3);
                c.appendListNode().set(4);
            });
        });

        final TomlConfigurationLoader loader = TomlConfigurationLoader.builder()
                .path(target)
                .set(JTomlOption.LINE_SEPARATOR, LineSeparator.LF)
                .build();

        loader.save(node);

        assertEquals(readLines(this.getClass().getResource("write-expected.toml")), Files.readAllLines(target, StandardCharsets.UTF_8));
    }

    @Test
    void nativeTypesRoundTrip(final @TempDir Path tempDir) throws IOException {
        final Path target = tempDir.resolve("native-types.toml");
        final ConfigurationLoader<BasicConfigurationNode> loader = TomlConfigurationLoader.builder()
                .path(target)
                .set(JTomlOption.LINE_SEPARATOR, LineSeparator.LF)
                .build();

        final BasicConfigurationNode node = loader.load();
        final NativeTypesTestConfig data = Faker.create(NativeTypesTestConfig.class);
        node.set(data);
        loader.save(node);
        final NativeTypesTestConfig roundTripped = loader.load().get(NativeTypesTestConfig.class);
        assertEquals(data, roundTripped);
    }

    @ConfigSerializable
    public static final class NativeTypesTestConfig {
        String string ;
        Boolean bool;
        Integer integer;
        Float floatNum;
        OffsetDateTime offsetDateTime;
        LocalDateTime localDateTime;
        LocalDate localDate;
        LocalTime localTime;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            NativeTypesTestConfig that = (NativeTypesTestConfig) o;
            return Objects.equals(string, that.string) && Objects.equals(bool, that.bool) && Objects.equals(integer, that.integer) && Objects.equals(floatNum, that.floatNum) && Objects.equals(offsetDateTime, that.offsetDateTime) && Objects.equals(localDateTime, that.localDateTime) && Objects.equals(localDate, that.localDate) && Objects.equals(localTime, that.localTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(string, bool, integer, floatNum, offsetDateTime, localDateTime, localDate, localTime);
        }
    }

    private static List<String> readLines(final URL source) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(source.openStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.toList());
        }
    }
}
