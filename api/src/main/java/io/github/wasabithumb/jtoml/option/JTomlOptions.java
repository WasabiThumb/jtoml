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

package io.github.wasabithumb.jtoml.option;

import io.github.wasabithumb.jtoml.util.Buildable;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * An immutable map of {@link JTomlOption}s and their associated values.
 * If a mapping does not exist, the {@link JTomlOption#defaultValue() default value} for
 * that option is reported.
 * @see #defaults()
 * @see #builder()
 */
public final class JTomlOptions implements Buildable<JTomlOptions> {

    private static final JTomlOption<?>[] UNIVERSE = JTomlOption.values();
    private static final JTomlOptions DEFAULTS = new JTomlOptions(new Object[0]);

    /**
     * Reports an empty {@link JTomlOptions} instance,
     * always reporting the {@link JTomlOption#defaultValue() default value}
     * for any option.
     */
    @Contract(pure = true)
    public static JTomlOptions defaults() {
        return DEFAULTS;
    }

    /**
     * Creates a new {@link Builder builder} for the purposes
     * of creating a custom {@link JTomlOptions} instance.
     */
    @Contract("-> new")
    public static Builder builder() {
        return new Builder();
    }

    //

    private final @Nullable Object[] values;

    private JTomlOptions(@Nullable Object [] values) {
        this.values = values;
    }

    //

    @Contract(pure = true)
    public <T> T get(JTomlOption<T> option) {
        final int o = option.ordinal();
        if (o < 0 || o >= this.values.length) return option.defaultValue();
        Object obj = this.values[o];
        return (obj == null) ? option.defaultValue() : option.valueClass().cast(obj);
    }

    @Contract(pure = true)
    public boolean get(JTomlOption.Bool option) {
        return this.get((JTomlOption<Boolean>) option);
    }

    @Override
    public Builder toBuilder() {
        Builder ret = new Builder();
        int max = -1;
        for (int i = 0; i < this.values.length; i++) {
            Object value = this.values[i];
            if (value == null) continue;
            ret.values[i] = value;
            max = i;
        }
        ret.max = max;
        return ret;
    }

    @Override
    public int hashCode() {
        int h = 7;
        for (JTomlOption<?> opt : UNIVERSE) {
            h = 31 * h + Objects.hashCode(this.get(opt));
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JTomlOptions)) return false;
        if (this == obj) return true;
        JTomlOptions qual = (JTomlOptions) obj;
        for (JTomlOption<?> opt : UNIVERSE) {
            if (!Objects.equals(this.get(opt), qual.get(opt))) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JTomlOptions[");
        JTomlOption<?> opt;
        for (int i = 0; i < UNIVERSE.length; i++) {
            if (i != 0) sb.append(",");
            opt = UNIVERSE[i];
            sb.append("\n\t")
                    .append(opt.name())
                    .append(" = ")
                    .append(this.get(opt));
        }
        sb.append("\n]");
        return sb.toString();
    }

    //

    public static final class Builder implements Buildable.Builder<JTomlOptions> {

        private final @Nullable Object[] values = new Object[UNIVERSE.length];
        private int max               = -1;
        private Builder() { }

        //

        @Contract(value = "_, _ -> this", mutates = "this")
        public <T> Builder set(JTomlOption<T> key, @Nullable T value) throws IllegalArgumentException {
            final int idx = key.ordinal();
            if (idx < 0 || idx >= UNIVERSE.length) {
                throw new IllegalStateException("Illegal ordinal (" + idx + ")");
            }
            if (value != null && !key.isLegal(value)) {
                throw new IllegalArgumentException("Illegal value (" + value + ") for " + key.name());
            }
            this.values[idx] = value;
            if (idx >= this.max) {
                if (value == null) {
                    int newMax = -1;
                    for (int i = idx - 1; i >= 0; i--) {
                        if (this.values[i] == null) continue;
                        newMax = i;
                        break;
                    }
                    this.max = newMax;
                } else {
                    this.max = idx;
                }
            }
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public Builder unset(JTomlOption<?> key) {
            return this.set(key, null);
        }

        @Contract("-> new")
        @Override
        public JTomlOptions build() {
            int count = this.max + 1;
            Object[] cpy = new Object[count];
            System.arraycopy(this.values, 0, cpy, 0, count);
            return new JTomlOptions(cpy);
        }

    }

}
