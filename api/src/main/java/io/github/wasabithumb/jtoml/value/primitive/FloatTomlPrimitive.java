package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

@ApiStatus.Internal
final class FloatTomlPrimitive extends AbstractTomlPrimitive<Double> {

    private static final DecimalFormat DF = new DecimalFormat("0.###############");

    //

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
        if (this.value == Double.POSITIVE_INFINITY) return "inf";
        if (this.value == Double.NEGATIVE_INFINITY) return "-inf";
        if (Double.isNaN(this.value)) return "nan";
        if ((this.value % 1) == 0) {
            if (Double.doubleToLongBits(this.value) == -9223372036854775808L) return "-0";
            return Long.toString((long) this.value);
        }
        return DF.format(this.value);
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
