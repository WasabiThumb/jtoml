package io.github.wasabithumb.jtoml.io.target;

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class WriterCharTarget extends FilterWriter implements CharTarget {

    public static @NotNull WriterCharTarget of(@NotNull OutputStream out) {
        return new WriterCharTarget(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    //

    public WriterCharTarget(@NotNull Writer out) {
        super(out);
    }

    //

    @Override
    public void put(int c) throws TomlException {
        try {
            this.out.write(c);
        } catch (IOException e) {
            TomlIOException.rethrow(e);
        }
    }

    @Override
    public void put(char c) throws TomlException {
        this.put((int) c);
    }

    @Override
    public void flush() throws TomlIOException {
        try {
            super.flush();
        } catch (IOException e) {
            TomlIOException.rethrow(e);
        }
    }

    @Override
    public void close() throws TomlIOException {
        try {
            super.close();
        } catch (IOException e) {
            TomlIOException.rethrow(e);
        }
    }

}
