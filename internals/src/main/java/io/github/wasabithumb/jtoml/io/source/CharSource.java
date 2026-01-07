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
