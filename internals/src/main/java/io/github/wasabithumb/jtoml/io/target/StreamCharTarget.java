package io.github.wasabithumb.jtoml.io.target;

import io.github.wasabithumb.jtoml.except.TomlIOException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public final class StreamCharTarget extends OutputStreamWriter implements CharTarget {

    public StreamCharTarget(@NotNull OutputStream out) {
        super(out, StandardCharsets.UTF_8);
    }

    //

    public void writeBOM() {
        this.put(0xFEFF);
    }

    @Override
    public void put(int c) throws TomlIOException {
        try {
            this.write(c);
        } catch (IOException e) {
            TomlIOException.rethrow(e);
        }
    }

    @Override
    public void put(char c) throws TomlIOException {
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
