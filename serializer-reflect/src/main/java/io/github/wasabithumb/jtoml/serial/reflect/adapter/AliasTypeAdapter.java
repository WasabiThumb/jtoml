package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

final class AliasTypeAdapter<T, F> implements TypeAdapter<T> {

    private final Class<T> typeClass;
    private final TypeAdapter<F> foreign;
    private final Function<F, T> toNative;
    private final Function<T, F> toForeign;

    AliasTypeAdapter(
            @NotNull Class<T> typeClass,
            @NotNull TypeAdapter<F> foreign,
            @NotNull Function<F, T> toNative,
            @NotNull Function<T, F> toForeign
    ) {
        this.typeClass = typeClass;
        this.foreign = foreign;
        this.toNative = toNative;
        this.toForeign = toForeign;
    }

    //

    @Override
    public @NotNull Class<T> typeClass() {
        return this.typeClass;
    }

    @Override
    public @NotNull T toJava(@NotNull TomlPrimitive toml) {
        return this.toNative.apply(this.foreign.toJava(toml));
    }

    @Override
    public @NotNull TomlPrimitive toToml(@NotNull T java) {
        return this.foreign.toToml(this.toForeign.apply(java));
    }

}
