package io.github.wasabithumb.jtoml.serial;

import io.github.wasabithumb.jtoml.JToml;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public abstract class TomlSerializerService {

    public abstract boolean canSerializeTo(@NotNull Class<?> outType);

    public abstract boolean canDeserializeFrom(@NotNull Class<?> inType);

    public abstract <T> @NotNull TomlSerializer<?, T> getSerializer(@NotNull JToml instance, @NotNull Class<T> outType);

    public abstract <T> @NotNull TomlSerializer<T, ?> getDeserializer(@NotNull JToml instance, @NotNull Class<T> inType);

}
