package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class BooleanTomlPrimitive extends AbstractTomlPrimitive<Boolean> {

    public static final BooleanTomlPrimitive TRUE = new BooleanTomlPrimitive(true);
    public static final BooleanTomlPrimitive FALSE = new BooleanTomlPrimitive(false);

    //

    private final boolean value;

    private BooleanTomlPrimitive(boolean value) {
        this.value = value;
    }

    //

    @Override
    public @NotNull TomlPrimitiveType type() {
        return TomlPrimitiveType.BOOLEAN;
    }

    @Override
    public @NotNull Boolean value() {
        return this.value;
    }

    @Override
    public @NotNull String asString() {
        return this.value ? "true" : "false";
    }

    @Override
    public boolean asBoolean() {
        return this.value;
    }

    @Override
    public long asLong() {
        return this.value ? 1L : 0L;
    }

    @Override
    public double asDouble() {
        return this.value ? 1d : 0d;
    }

}
