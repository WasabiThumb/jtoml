package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

final class BasicTypeAdapter<T> implements TypeAdapter<T> {

    private final Class<T> typeClass;
    private final Function<TomlPrimitive, T> toJava;
    private final Function<T, TomlPrimitive> toToml;

    BasicTypeAdapter(
            @NotNull Class<T> typeClass,
            @NotNull Function<TomlPrimitive, T> toJava,
            @NotNull Function<T, TomlPrimitive> toToml
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
    public @NotNull T toJava(@NotNull TomlPrimitive toml) {
        return this.toJava.apply(toml);
    }

    @Override
    public @NotNull TomlPrimitive toToml(@NotNull T java) {
        return this.toToml.apply(java);
    }

}
