package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;

@ApiStatus.Internal
final class FloatTomlPrimitive extends AbstractTomlPrimitive<Double> {

    private static final ThreadLocal<NumberFormat> NUMBER_FORMAT = ThreadLocal.withInitial(() -> {
        NumberFormat df = NumberFormat.getInstance(Locale.ROOT);
        df.setMaximumFractionDigits(15);
        df.setMinimumFractionDigits(1);
        df.setGroupingUsed(false);
        return df;
    });

    private static @NotNull String autoChars(double value) {
        if (value == Double.POSITIVE_INFINITY) return "inf";
        if (value == Double.NEGATIVE_INFINITY) return "-inf";
        if (Double.isNaN(value)) return "nan";
        if (Double.doubleToLongBits(value) == -9223372036854775808L) return "-0.0";
        return NUMBER_FORMAT.get().format(value);
    }

    //

    private final double value;
    private final String chars;

    private FloatTomlPrimitive(@NotNull Comments comments, double value, @NotNull String chars) {
        super(comments);
        this.value = value;
        this.chars = chars;
    }

    /** Called by {@code UnsafePrimitives} in {@code jtoml-internals} */
    public FloatTomlPrimitive(double value, @NotNull String chars) {
        this(Comments.empty(), value, chars);
    }

    /** Called by {@code TomlPrimitive#copyOf} */
    public FloatTomlPrimitive(@NotNull Comments comments, double value) {
        this(comments, value, autoChars(value));
    }

    /** Called by {@code TomlPrimitive#of} */
    public FloatTomlPrimitive(double value) {
        this(Comments.empty(), value, autoChars(value));
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
