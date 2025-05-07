package io.github.wasabithumb.jtoml.option;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable map of {@link JTomlOption}s and their associated values.
 * If a mapping does not exist, the {@link JTomlOption#defaultValue() default value} for
 * that option is reported.
 * @see #defaults()
 * @see #builder()
 */
public final class JTomlOptions {

    private static final JTomlOptions DEFAULTS = new JTomlOptions(new Object[0]);

    @Contract(pure = true)
    public static @NotNull JTomlOptions defaults() {
        return DEFAULTS;
    }

    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    //

    private final Object[] values;
    private JTomlOptions(@Nullable Object @NotNull [] values) {
        this.values = values;
    }

    //

    @Contract(pure = true)
    public <T> @NotNull T get(@NotNull JTomlOption<T> option) {
        final int o = option.ordinal();
        if (o < 0 || o >= this.values.length) return option.defaultValue();
        Object obj = this.values[o];
        return (obj == null) ? option.defaultValue() : option.valueClass().cast(obj);
    }

    @Contract(pure = true)
    public boolean get(@NotNull JTomlOption.Bool option) {
        return this.get((JTomlOption<Boolean>) option);
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder("JTomlOptions[");
        JTomlOption<?>[] opts = JTomlOption.values();
        JTomlOption<?> opt;
        for (int i=0; i < opts.length; i++) {
            if (i != 0) sb.append(",");
            opt = opts[i];
            sb.append("\n\t")
                    .append(opt.name())
                    .append(" = ")
                    .append(this.get(opt));
        }
        sb.append("\n]");
        return sb.toString();
    }

    //

    public static final class Builder {

        private static final int CAPACITY = JTomlOption.values().length;

        //

        private final Object[] values = new Object[CAPACITY];
        private int max               = -1;
        private Builder() { }

        //

        @Contract(value = "_, _ -> this", mutates = "this")
        public <T> @NotNull Builder set(@NotNull JTomlOption<T> key, @Nullable T value) throws IllegalArgumentException {
            final int idx = key.ordinal();
            if (idx < 0 || idx >= CAPACITY) {
                throw new IllegalStateException("Illegal ordinal (" + idx + ")");
            }
            if (value != null && !key.isLegal(value)) {
                throw new IllegalArgumentException("Illegal value (" + value + ") for " + key.name());
            }
            this.values[idx] = value;
            if (idx > this.max) this.max = idx;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder unset(@NotNull JTomlOption<?> key) {
            return this.set(key, null);
        }

        @Contract("-> new")
        public @NotNull JTomlOptions build() {
            int count = this.max + 1;
            Object[] cpy = new Object[count];
            System.arraycopy(this.values, 0, cpy, 0, count);
            return new JTomlOptions(cpy);
        }

    }

}
