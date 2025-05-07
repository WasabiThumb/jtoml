package io.github.wasabithumb.jtoml.serial.gson;

import com.google.gson.JsonObject;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.serial.TomlSerializerService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class GsonTomlSerializerService extends TomlSerializerService {

    @Override
    public boolean canSerializeTo(@NotNull Class<?> outType) {
        return JsonObject.class.equals(outType);
    }

    @Override
    public boolean canDeserializeFrom(@NotNull Class<?> inType) {
        return JsonObject.class.equals(inType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T> TomlSerializer<?, T> getSerializer(@NotNull JToml instance, @NotNull Class<T> outType) {
        if (!JsonObject.class.equals(outType)) throw new IllegalArgumentException();
        return (TomlSerializer<?, T>) GsonTomlSerializer.instance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T> TomlSerializer<T, ?> getDeserializer(@NotNull JToml instance, @NotNull Class<T> inType) {
        if (!JsonObject.class.equals(inType)) throw new IllegalArgumentException();
        return (TomlSerializer<T, ?>) GsonTomlSerializer.instance();
    }

}
