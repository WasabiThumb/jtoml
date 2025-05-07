package io.github.wasabithumb.jtoml.io.source;

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.Closeable;
import java.io.EOFException;

public interface CharSource extends Closeable {

    @Range(from=-1, to=0xFFFF) int next() throws TomlException;

    default char nextChar() throws TomlException {
        int n = this.next();
        if (n == -1) TomlIOException.rethrow(new EOFException("Unexpected end of stream"));
        return (char) n;
    }

    default int next(char @NotNull [] dest) throws TomlException {
        int count = 0;
        int c;
        while (count < dest.length) {
            c = this.next();
            if (c == -1) break;
            dest[count++] = (char) c;
        }
        return count;
    }

    @Override
    void close() throws TomlException;

}
