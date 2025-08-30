package io.github.wasabithumb.jtoml.serial.plain;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class PlainTextTomlSerializer implements TomlSerializer.Symmetric<String> {

    private final JToml instance;

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
