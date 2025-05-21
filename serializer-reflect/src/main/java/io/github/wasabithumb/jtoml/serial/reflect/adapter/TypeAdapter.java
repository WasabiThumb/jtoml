package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@ApiStatus.Internal
public interface TypeAdapter<T> {

    @Contract("_, _, _ -> new")
    static <O> @NotNull TypeAdapter<O> create(
            @NotNull Class<O> typeClass,
            @NotNull Function<TomlPrimitive, O> toJava,
            @NotNull Function<O, TomlPrimitive> toToml
    ) {
        return new BasicTypeAdapter<>(typeClass, toJava, toToml);
    }

    //

    @NotNull Class<T> typeClass();

    @NotNull T toJava(@NotNull TomlPrimitive toml);

    @NotNull TomlPrimitive toToml(@NotNull T java);

}
