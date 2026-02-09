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

package io.github.wasabithumb.jtoml.key.convention;

import io.github.wasabithumb.jtoml.key.TomlKey;
import org.jetbrains.annotations.NotNull;

/**
 * Determines how a given identifier may be
 * converted to a {@link TomlKey}. This is currently
 * used by the reflect serializer to map fields and
 * record components to their corresponding keys in
 * absence of an explicit mapping. Owing to the general
 * use cases of this interface, the
 * {@link StandardKeyConvention standard conventions}
 * appropriately expect input to be strict ASCII in
 * {@code camelCase}.
 * @see StandardKeyConvention
 */
@FunctionalInterface
public interface KeyConvention {

    /**
     * Adapts the given identifier into a {@link TomlKey}
     * based on the convention.
     */
    @NotNull TomlKey toToml(@NotNull String key);
    
}
