/*
 * Copyright 2026 Xavier Pedraza
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
import java.util.regex.Pattern;

@ApiStatus.Internal
final class FloatTomlPrimitive extends AbstractTomlPrimitive<Double> {

    private static final double NEGATIVE_NAN = Double.longBitsToDouble(0xfff8000000000000L);
    private static final Pattern NORMAL_FLOAT_PATTERN = Pattern.compile("^[+-]?([1-9]|0(?=[.eE]))(_?\\d)*(\\.\\d(_?\\d)*)?([eE][+-]?\\d(_?\\d)*)?$");

    private static final ThreadLocal<NumberFormat> NUMBER_FORMAT = ThreadLocal.withInitial(() -> {
        NumberFormat df = NumberFormat.getInstance(Locale.ROOT);
        df.setMaximumFractionDigits(15);
        df.setMinimumFractionDigits(1);
        df.setGroupingUsed(false);
        return df;
    });

    private static @NotNull String autoChars(double value) {
        long bits = Double.doubleToRawLongBits(value);
        if (bits == 0x8000000000000000L) return "-0.0";
        if ((bits & 0x7ff0000000000000L) == 0x7ff0000000000000L) {
            if ((bits & 0x000fffffffffffffL) == 0L) {
                return (bits & 0x8000000000000000L) == 0x8000000000000000L ?
                        "-inf" : "inf";
            } else {
                return (bits & 0x8000000000000000L) == 0x8000000000000000L ?
                        "-nan" : "nan";
            }
        }
        return NUMBER_FORMAT.get().format(value);
    }

    static @NotNull FloatTomlPrimitive parse(@NotNull CharSequence str) throws IllegalArgumentException {
        final int len = str.length();
        if (len == 0) {
            throw new IllegalArgumentException("Cannot parse empty string as TOML float");
        }

        // Check for special floats (+/- inf, nan)
        if (len > 2) {
            int start = 0;
            char c = str.charAt(start);
            boolean negative = false;
            if (c == '+') {
                c = str.charAt(++start);
            } else if (c == '-') {
                c = str.charAt(++start);
                negative = true;
            }
            if (c == 'i') {
                if (str.charAt(start + 1) == 'n' &&
                        start + 3 == len &&
                        str.charAt(start + 2) == 'f'
                ) {
                    return new FloatTomlPrimitive(
                            negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY,
                            str.toString()
                    );
                }
            } else if (c == 'n') {
                if (str.charAt(start + 1) == 'a' &&
                    start + 3 == len &&
                    str.charAt(start + 2) == 'n'
                ) {
                    return new FloatTomlPrimitive(
                            negative ? NEGATIVE_NAN : Double.NaN,
                            str.toString()
                    );
                }
            }
        }

        // This approach may seem inefficient, but delegating
        // to Java's parseDouble avoids significant headaches
        // (see #76). We still can't pass it directory to parseDouble
        // since TOML's rules still need to be followed.
        // So first, we check if it's valid, then strip underscores,
        // then pass to parseDouble.

        if (!NORMAL_FLOAT_PATTERN.matcher(str).matches()) {
            throw new IllegalArgumentException("TOML float string (" + str + ") does not match pattern");
        }

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == '_') continue;
            sb.append(c);
        }

        String stripped = sb.toString();
        double value;
        try {
            value = Double.parseDouble(stripped);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("TOML float string (" + str + ") could not be parsed", e);
        }

        return new FloatTomlPrimitive(value, str.toString());
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
