package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;

@ApiStatus.Internal
final class FloatTomlPrimitive extends AbstractTomlPrimitive<Double> {

    private static @NotNull String autoChars(double value) {
        if (value == Double.POSITIVE_INFINITY) return "inf";
        if (value == Double.NEGATIVE_INFINITY) return "-inf";
        if (Double.isNaN(value)) return "nan";
        if ((value % 1) == 0) {
            if (Double.doubleToLongBits(value) == -9223372036854775808L) return "-0";
            return Long.toString((long) value);
        }
        NumberFormat decimalFormat = NumberFormat.getInstance(Locale.ROOT);
        decimalFormat.setMaximumFractionDigits(15);
        return decimalFormat.format(value);
    }

    //

    private final double value;
    private final String chars;

    /** Called by {@code UnsafePrimitives} in {@code jtoml-internals} */
    public FloatTomlPrimitive(double value, @NotNull String chars) {
        this.value = value;
        this.chars = chars;
    }

    public FloatTomlPrimitive(double value) {
        this(value, autoChars(value));
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
        return this.chars;
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
