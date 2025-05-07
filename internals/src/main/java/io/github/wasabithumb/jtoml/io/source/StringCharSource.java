package io.github.wasabithumb.jtoml.io.source;

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.EOFException;

public final class StringCharSource implements CharSource {

    private final String string;
    private int head;

    public StringCharSource(@NotNull String string) {
        this.string = string;
        this.head = 0;
    }

    //

    @Override
    public @Range(from = -1, to = 0xFFFF) int next() throws TomlException {
        if (this.head >= this.string.length()) return -1;
        return this.string.charAt(this.head++);
    }

    @Override
    public char nextChar() throws TomlException {
        if (this.head >= this.string.length()) TomlIOException.rethrow(new EOFException("Unexpected end of string"));
        return this.string.charAt(this.head++);
    }

    @Override
    public void close() throws TomlException { }

}
