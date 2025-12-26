package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@ApiStatus.Internal
final class TypeAdapterImpl<T> implements TypeAdapter<T> {

    private final Class<T> typeClass;
    private final Function<TomlValue, T> toJava;
    private final Function<T, TomlValue> toToml;

    TypeAdapterImpl(
            @NotNull Class<T> typeClass,
            @NotNull Function<TomlValue, T> toJava,
            @NotNull Function<T, TomlValue> toToml
    ) {
        this.typeClass = typeClass;
        this.toJava = toJava;
        this.toToml = toToml;
    }

    //

    @Override
    public @NotNull Class<T> typeClass() {
        return this.typeClass;
    }

    @Override
    public @NotNull T toJava(@NotNull TomlValue toml) {
        return this.toJava.apply(toml);
    }

    @Override
    public @NotNull TomlValue toToml(@NotNull T java) {
        return this.toToml.apply(java);
    }

}
