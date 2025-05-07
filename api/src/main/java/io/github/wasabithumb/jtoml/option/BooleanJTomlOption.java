package io.github.wasabithumb.jtoml.option;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class BooleanJTomlOption extends AbstractJTomlOption<Boolean> implements JTomlOption.Bool {

    public static @NotNull BooleanJTomlOption of(
            @NotNull String name,
            boolean defaultValue
    ) {
        return new BooleanJTomlOption(name, defaultValue);
    }

    //

    public BooleanJTomlOption(@NotNull String name, @NotNull Boolean defaultValue) {
        super(name, defaultValue);
    }

}
