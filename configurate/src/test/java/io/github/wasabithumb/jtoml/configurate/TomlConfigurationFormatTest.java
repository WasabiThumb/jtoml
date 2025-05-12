package io.github.wasabithumb.jtoml.configurate;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationFormat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TomlConfigurationFormatTest {

    @Test
    void testTomlFormatPresent() {
        final @Nullable ConfigurationFormat format = ConfigurationFormat.forExtension("toml");
        assertNotNull(format);
    }

    @Test
    void testLoadToml() throws ConfigurateException {
        final @Nullable ConfigurationFormat format = ConfigurationFormat.forExtension("toml");
        final ConfigurationNode node = format.create(this.getClass().getResource("simple.toml")).load();
        assertTrue(node.node("test").getBoolean());
    }

}