package io.github.wasabithumb.jtoml.serial.gson;

import com.google.gson.JsonObject;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.serial.TomlSerializerFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class GsonTomlSerializerFactory extends TomlSerializerFactory {

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> Result<?, T> fromToml(@NotNull JToml instance, @NotNull Class<T> outType) {
        if (!JsonObject.class.equals(outType)) return Result.invalid();
        return Result.valid(() -> (TomlSerializer<?, T>) GsonTomlSerializer.instance());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> Result<T, ?> toToml(@NotNull JToml instance, @NotNull Class<T> inType) {
        if (!JsonObject.class.equals(inType)) return Result.invalid();
        return Result.valid(() -> (TomlSerializer<T, ?>) GsonTomlSerializer.instance());
    }

}
