/*
 * Copyright 2026 Xavier Pedraza
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

package io.github.wasabithumb.jtoml.document;

import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The top-level table read from a TOML file/string.
 * Mostly identical to a regular table, but may
 * hold {@link #issues() issues}.
 */
@ApiStatus.NonExtendable
public interface TomlDocument extends TomlTable {

    /**
     * Issues generated when parsing this document.
     * This will be empty unless
     * {@link io.github.wasabithumb.jtoml.option.JTomlOption#ERROR_RECOVERY error recovery}
     * is enabled.
     */
    @NotNull TomlIssues issues();

}
