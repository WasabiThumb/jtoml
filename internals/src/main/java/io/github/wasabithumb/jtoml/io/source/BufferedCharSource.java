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
import io.github.wasabithumb.jtoml.except.parse.TomlLocalParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class BufferedCharSource implements CharSource {

    private static final int BUFFER_SIZE = 8;

    //

    private final CharSource backing;
    private final char[] buf;
    private int ir;
    private int iw;
    private int ln;
    private int cn;

    public BufferedCharSource(@NotNull CharSource backing) {
        this.backing = backing;
        this.buf = new char[BUFFER_SIZE];
        this.ir = 0;
        this.iw = 0;
        this.ln = 0;
        this.cn = 0;
    }

    //

    public int peek() throws TomlException {
        int c = this.backing.next();
        if (c == -1) return -1;

        int next = (this.iw + 1) % BUFFER_SIZE;
        if (next == this.ir) throw new AssertionError("Buffer overflow");
        this.buf[this.iw] = (char) c;
        this.iw = next;

        return c;
    }

    /**
     * Reads past any and all whitespace before the first non-whitespace character
     * @return False if EOF
     */
    public boolean skipWhitespace() throws TomlException {
        int next;
        while (true) {
            next = this.next();
            if (next == -1) return false;
            if (next != ' ' && next != '\t') {
                this.buf[this.iw] = (char) next;
                this.iw = (this.iw + 1) % BUFFER_SIZE;
                return true;
            }
        }
    }

    /**
     * Reads past whitespace and comments until a newline or EOF is found
     * @param comment True if known to be inside a comment. If false,
     *                comments may still be entered.
     * @return The comment read, if any
     */
    @Contract("true -> !null")
    public @Nullable String finishExpression(boolean comment) throws TomlException {
        int next;
        StringBuilder commentBuffer = comment ?
                new StringBuilder() :
                null;

        while (true) {
            next = this.next();
            if (next == -1) break;
            if (next == '\n') break;
            if (next == '\r') {
                int lf = this.next();
                if (lf != '\n') this.raise("Read CR without matching LF");
                break;
            }
            if (comment) {
                if (next == ' ' || next == '\t') {
                    if (commentBuffer.length() == 0) continue;
                } else if (next < ' ' || next == 0x7F) {
                    this.raise("Control character (" + next + ") is not allowed in comment");
                }
                commentBuffer.append((char) next);
                continue;
            }
            if (next == ' ' || next == '\t') continue;
            if (next == '#') {
                comment = true;
                commentBuffer = new StringBuilder();
                continue;
            }
            this.raise("Expected whitespace or comment, got character (" + next + ")");
        }

        return comment ?
                commentBuffer.toString() :
                null;
    }

    @Contract("_ -> fail")
    public void raise(@NotNull String message) throws TomlLocalParseException {
        throw new TomlLocalParseException(message, this.ln, this.cn);
    }

    @Contract("_, _ -> fail")
    public void raise(@NotNull String message, @Nullable Throwable cause) throws TomlLocalParseException {
        throw new TomlLocalParseException(message, cause, this.ln, this.cn);
    }

    //

    @Override
    public @Range(from = -1, to = 0xFFFF) int next() throws TomlException {
        int n;
        if (this.ir != this.iw) {
            n = this.buf[this.ir];
            this.ir = (this.ir + 1) % BUFFER_SIZE;
        } else {
            n = this.backing.next();
        }
        if (n == '\n') {
            this.ln++;
            this.cn = 0;
        } else {
            this.cn++;
        }
        return n;
    }

    @Override
    public char nextChar() throws TomlException {
        char n;
        if (this.ir != this.iw) {
            n = this.buf[this.ir];
            this.ir = (this.ir + 1) % BUFFER_SIZE;
        } else {
            n = this.backing.nextChar();
        }
        if (n == '\n') {
            this.ln++;
            this.cn = 0;
        } else {
            this.cn++;
        }
        return n;
    }

    @Override
    public void close() throws TomlException {
        this.backing.close();
    }

}
