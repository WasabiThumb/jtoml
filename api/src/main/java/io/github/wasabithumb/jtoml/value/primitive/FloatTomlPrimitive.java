/*
 * Copyright 2025 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
final class FloatTomlPrimitive extends AbstractTomlPrimitive<Double> {

    private static final Pattern PARSE_PATTERN =
            Pattern.compile("^([-+]?(?:inf|nan))|([-+]?)([1-9]\\d*)(?:\\.(\\d+))?(?:e([-+]?\\d+))?$");

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

    static @NotNull FloatTomlPrimitive parse(@NotNull String string) throws IllegalArgumentException {
        Matcher m = PARSE_PATTERN.matcher(string);
        if (!m.matches()) throw new IllegalArgumentException("Invalid float string: " + string);

        String special = m.group(1);
        if (special != null && !special.isEmpty()) {
            switch (special) {
                case "-inf":
                    return new FloatTomlPrimitive(Double.NEGATIVE_INFINITY, special);
                case "+inf":
                case "inf":
                    return new FloatTomlPrimitive(Double.POSITIVE_INFINITY, special);
                default:
                    return new FloatTomlPrimitive(Double.NaN, special);
            }
        }

        boolean negative = false;
        String signText = m.group(2);
        if (signText != null && !signText.isEmpty()) {
            negative = signText.charAt(0) == '-';
        }

        long intPart = Long.parseLong(m.group(3));
        double frac = 0d;
        String fracText = m.group(4);
        if (fracText != null && !fracText.isEmpty()) {
            frac = Double.parseDouble("0." + fracText);
        }

        long exp = 0L;
        String expText = m.group(5);
        if (expText != null && !expText.isEmpty()) {
            exp = Long.parseLong(expText);
        }

        double ret;
        if (exp != 0L) {
            double scale = Math.pow(10, exp);
            ret = (scale * intPart) + (scale * frac);
        } else {
            ret = ((double) intPart) + frac;
        }
        if (negative) ret = -ret;
        return new FloatTomlPrimitive(ret, string);
    }

    //

    private final double value;
    private final String chars;

    private FloatTomlPrimitive(@NotNull Comments comments, double value, @NotNull String chars) {
        super(comments);
        this.value = value;
        this.chars = chars;
    }

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
