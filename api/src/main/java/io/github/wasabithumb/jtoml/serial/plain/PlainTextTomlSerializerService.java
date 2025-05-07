package io.github.wasabithumb.jtoml.serial.plain;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.serial.TomlSerializerService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class PlainTextTomlSerializerService extends TomlSerializerService {

    @Override
    public boolean canSerializeTo(@NotNull Class<?> outType) {
        return String.class.equals(outType);
    }

    @Override
    public boolean canDeserializeFrom(@NotNull Class<?> inType) {
        return String.class.equals(inType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> TomlSerializer<?, T> getSerializer(@NotNull JToml instance, @NotNull Class<T> outType) {
        if (!String.class.equals(outType)) throw new IllegalArgumentException();
        return (TomlSerializer<?, T>) new PlainTextTomlSerializer(instance);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> TomlSerializer<T, ?> getDeserializer(@NotNull JToml instance, @NotNull Class<T> inType) {
        if (!String.class.equals(inType)) throw new IllegalArgumentException();
        return (TomlSerializer<T, ?>) new PlainTextTomlSerializer(instance);
    }

}
