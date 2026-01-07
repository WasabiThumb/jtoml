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
