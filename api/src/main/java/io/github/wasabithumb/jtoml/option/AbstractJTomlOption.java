package io.github.wasabithumb.jtoml.option;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
abstract class AbstractJTomlOption<T> implements JTomlOption<T> {

    private static int HEAD = 0;
    private static synchronized int nextOrdinal() {
        return HEAD++;
    }

    //

    protected final int ordinal;
    protected final String name;
    protected final T defaultValue;

    protected AbstractJTomlOption(@NotNull String name, @NotNull T defaultValue) {
        this.ordinal = nextOrdinal();
        this.name = name;
        this.defaultValue = defaultValue;
    }

    //

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull T defaultValue() {
        return this.defaultValue;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.ordinal());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JTomlOption<?>)) return false;
        return this.ordinal() == ((JTomlOption<?>) obj).ordinal();
    }

    @Override
    public @NotNull String toString() {
        return this.name();
    }

}
