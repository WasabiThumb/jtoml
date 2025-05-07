package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class FloatTomlPrimitive extends AbstractTomlPrimitive<Double> {

    private final double value;

    public FloatTomlPrimitive(double value) {
        this.value = value;
    }

    //

    @Override
    public @NotNull TomlPrimitiveType type() {
        return TomlPrimitiveType.FLOAT;
    }

    @Override
    public @NotNull Double value() {
        return this.value;
    }

    @Override
    public @NotNull String asString() {
        return Double.toString(this.value);
    }

    @Override
    public boolean asBoolean() {
        return this.value != 0d;
    }

    @Override
    public long asLong() {
        return (long) this.value;
    }

    @Override
    public double asDouble() {
        return this.value;
    }

}
