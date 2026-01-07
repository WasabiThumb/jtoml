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

package io.github.wasabithumb.jtoml.serial.plain;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.NotNull;

/**
 * A bare-bones serializer which uses a
 * {@link JToml} instance to convert
 * a TOML table to/from a {@link String}.
 * The {@link #fromToml(TomlTable) fromToml} method is
 * identical to {@link JToml#writeToString(TomlTable) writeToString}
 * and the {@link #toToml(String) toToml} method is
 * identical to {@link JToml#readFromString(String) readFromString}.
 */
public final class PlainTextTomlSerializer implements TomlSerializer.Symmetric<String> {

    private final JToml instance;

    /**
     * Creates a new plain text serializer,
     * deferring to the given {@link JToml} instance's
     * {@link JToml#readFromString(String) readFromString} and {@link JToml#writeToString(TomlTable) writeToString}
     * methods.
     */
    public PlainTextTomlSerializer(@NotNull JToml instance) {
        this.instance = instance;
    }

    //

    @Override
    public @NotNull Class<String> serialType() {
        return String.class;
    }

    @Override
    public @NotNull String fromToml(@NotNull TomlTable table) {
        return this.instance.writeToString(table);
    }

    @Override
    public @NotNull TomlTable toToml(@NotNull String data) {
        return this.instance.readFromString(data);
    }

    @Override
    public @NotNull String serialize(@NotNull TomlTable table) {
        return this.fromToml(table);
    }

    @Override
    public @NotNull TomlTable deserialize(@NotNull String data) {
        return this.toToml(data);
    }

}
