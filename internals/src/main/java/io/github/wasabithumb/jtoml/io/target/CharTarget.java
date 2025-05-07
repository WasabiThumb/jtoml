package io.github.wasabithumb.jtoml.io.target;

import io.github.wasabithumb.jtoml.except.TomlException;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

public interface CharTarget extends Closeable {

    void put(int c) throws TomlException;

    void put(char c) throws TomlException;

    default void put(@NotNull CharSequence cs) throws TomlException {
        for (int i=0; i < cs.length(); i++) this.put(cs.charAt(i));
    }

    @Override
    void close() throws TomlException;

}
