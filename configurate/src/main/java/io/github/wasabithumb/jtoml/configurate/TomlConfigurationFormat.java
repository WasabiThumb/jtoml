/*
 * Copyright 2025 Xavier Pedraza
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

package io.github.wasabithumb.jtoml.configurate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.CommentedConfigurationNode;
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
        CommentedConfigurationNode,
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
