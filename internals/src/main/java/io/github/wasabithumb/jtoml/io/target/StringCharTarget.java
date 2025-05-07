package io.github.wasabithumb.jtoml.io.target;

import org.jetbrains.annotations.NotNull;

public final class StringCharTarget implements CharTarget {

    private final StringBuilder backing = new StringBuilder();

    //

    @Override
    public void put(int c) {
        this.put((char) c);
    }

    @Override
    public void put(char c) {
        this.backing.append(c);
    }

    @Override
    public void put(@NotNull CharSequence cs) {
        this.backing.append(cs);
    }

    @Override
    public void close() { }

    @Override
    public @NotNull String toString() {
        return this.backing.toString();
    }

}
