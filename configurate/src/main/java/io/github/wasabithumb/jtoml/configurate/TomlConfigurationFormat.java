package io.github.wasabithumb.jtoml.configurate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationFormat;
import org.spongepowered.configurate.loader.ConfigurationFormat;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.util.Set;

/**
 * A {@link ConfigurationFormat} for the {@link TomlConfigurationLoader}.
 *
 * <p>This format should not be used directly, but instead accessed
 * through methods on {@link ConfigurationFormat}.</p>
 */
@DefaultQualifier(NonNull.class)
public final class TomlConfigurationFormat extends AbstractConfigurationFormat<
        BasicConfigurationNode,
        TomlConfigurationLoader,
        TomlConfigurationLoader.Builder
        > {

    private static final Set<String> SUPPORTED_EXTENSIONS = UnmodifiableCollections.toSet("toml");

    /**
     * For use by service loader only.
     */
    public TomlConfigurationFormat() {
        super("jtoml", TomlConfigurationLoader::builder, SUPPORTED_EXTENSIONS);
    }
}
