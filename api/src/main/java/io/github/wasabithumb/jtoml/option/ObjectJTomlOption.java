package io.github.wasabithumb.jtoml.option;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class ObjectJTomlOption<T> extends AbstractJTomlOption<T> {

    public static <R> @NotNull ObjectJTomlOption<R> of(
            @NotNull String name,
            @NotNull Class<R> valueClass,
            @NotNull R defaultValue
    ) {
        return new ObjectJTomlOption<>(name, valueClass, defaultValue);
    }

    //

    private final Class<T> valueClass;

    public ObjectJTomlOption(@NotNull String name, @NotNull Class<T> valueClass, @NotNull T defaultValue) {
        super(name, defaultValue);
        this.valueClass = valueClass;
    }

    //

    @Override
    public @NotNull Class<T> valueClass() {
        return this.valueClass;
    }

}
