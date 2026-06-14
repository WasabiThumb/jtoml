package io.github.wasabithumb.jtoml.serial.plain;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.serial.TomlSerializerFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class PlainTextTomlSerializerFactory extends TomlSerializerFactory {

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> Result<?, T> fromToml(@NotNull JToml instance, @NotNull Class<T> outType) {
        if (!String.class.equals(outType)) return Result.invalid("not String");
        return Result.valid(() -> (TomlSerializer<?, T>) new PlainTextTomlSerializer(instance));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> Result<T, ?> toToml(@NotNull JToml instance, @NotNull Class<T> inType) {
        if (!String.class.equals(inType)) return Result.invalid("not String");
        return Result.valid(() -> (TomlSerializer<T, ?>) new PlainTextTomlSerializer(instance));
    }

}
