/*
 * Copyright 2025 Xavier Pedraza
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
    public void put(@NotNull CharSequence cs) throws TomlException {
        try {
            if (cs instanceof String) {
                this.out.write((String) cs);
            } else {
                for (int i = 0; i < cs.length(); i++) {
                    this.out.write(cs.charAt(i));
                }
            }
        } catch (IOException e) {
            TomlIOException.rethrow(e);
        }
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
