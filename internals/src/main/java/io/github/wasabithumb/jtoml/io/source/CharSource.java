/*
 * Copyright 2026 Xavier Pedraza
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
import io.github.wasabithumb.jtoml.except.parse.TomlTruncatedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.Closeable;
import java.io.EOFException;

public interface CharSource extends Closeable {

    @Range(from=-1, to=0xFFFF) int next() throws TomlException;

    /**
     * @apiNote Prefer {@link #nextChar(String)} unless the presence of a
     * character is an invariant
     */
    default char nextChar() throws TomlException {
        return this.nextChar(null);
    }

    default char nextChar(@Nullable String expectation) throws TomlException {
        int n = this.next();
        if (n == -1) {
            if (expectation == null) expectation = "a character";
            throw new TomlTruncatedException("Expected " + expectation + ", got end of document");
        }
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
