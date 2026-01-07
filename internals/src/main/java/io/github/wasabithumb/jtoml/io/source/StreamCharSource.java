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
import io.github.wasabithumb.jtoml.except.parse.TomlBomException;
import io.github.wasabithumb.jtoml.except.parse.TomlCodingException;
import io.github.wasabithumb.jtoml.option.prop.OrderMarkPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class StreamCharSource extends FilterInputStream implements CharSource {

    private static final int BOM_ALLOWED  = 1;
    private static final int BOM_REQUIRED = 2;
    private static final int BOM_CHECKED  = 4;
    private static final int BOM_READ     = 8;

    @SuppressWarnings("fallthrough")
    private static int initialFlags(@NotNull OrderMarkPolicy bomPolicy) {
        int flags = 0;
        switch (bomPolicy) {
            case ALWAYS:
                flags |= BOM_REQUIRED;
            case IF_PRESENT:
                flags |= BOM_ALLOWED;
                break;
        }
        return flags;
    }

    //

    private int flags;
    private int carry;

    public StreamCharSource(@NotNull InputStream in, @NotNull OrderMarkPolicy bomPolicy) {
        super(in);
        this.flags = initialFlags(bomPolicy);
        this.carry = -1;
    }

    //

    public boolean didReadBOM() {
        return (this.flags & BOM_READ) != 0;
    }

    private @Range(from = -1, to = 255) int readSingle() throws TomlException {
        try {
            return this.in.read();
        } catch (IOException e) {
            TomlIOException.rethrow(e);
            return -1;
        }
    }

    private @Range(from = 0, to = 255) int readContinuation() throws TomlException {
        int b = this.readSingle();
        if (b == -1 || (b & 0xC0) != 0x80) throw new TomlCodingException("Truncated encoding");
        return b;
    }

    private @Range(from = -1, to = 0xFFFF) int readChar() throws TomlException {
        int carry = this.carry;
        if (carry != -1) {
            this.carry = -1;
            return carry;
        }

        int b0 = this.readSingle();
        if (b0 == -1) return -1;

        if ((b0 & 0x80) == 0) return b0; // U+0000 to U+007F

        if ((b0 & 0x40) == 0) {
            throw new TomlCodingException("Illegal first byte (continuation byte)");
        }

        if ((b0 & 0x20) == 0) {          // U+0080 to U+07FF
            if (b0 == 0xC0 || b0 == 0xC1) throw new TomlCodingException("Illegal byte (" + b0 + ")");
            int b1 = this.readContinuation();
            int c = ((b0 & 0x1F) << 6) | (b1 & 0x3F);
            if (c < 0x0080) throw new TomlCodingException("Overlong encoding");
            return c;
        }

        if ((b0 & 0x10) == 0) {          // U+0800 to U+FFFF
            int b1 = this.readContinuation();
            int b2 = this.readContinuation();
            int c = ((b0 & 0x0F) << 12) | ((b1 & 0x3F) << 6) | (b2 & 0x3F);
            if (c < 0x0800) throw new TomlCodingException("Overlong encoding");
            if (0xD800 <= c && c <= 0xDFFF) throw new TomlCodingException("Illegal codepoint (UTF-16 surrogate)");
            return c;
        }

        if ((b0 & 0x08) == 0) {          // U+010000 to U+10FFFF
            if (b0 >= 0xF5) throw new TomlCodingException("Illegal byte (" + b0 + ")");
            int b1 = this.readContinuation();
            int b2 = this.readContinuation();
            int b3 = this.readContinuation();
            int c = ((b0 & 0x07) << 18) | ((b1 & 0x3F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F);
            if (c < 0x010000) throw new TomlCodingException("Overlong encoding");
            if (c > 0x10FFFF) throw new TomlCodingException("Codepoint too large (" + c + ")");

            // Convert to surrogate pair
            c -= 0x10000;
            this.carry = (c & 0x3FF) | 0xDC00; // Low surrogate
            return (c >> 10) | 0xD800;         // High surrogate
        }

        throw new TomlCodingException("Illegal first byte (" + b0 + ")");
    }

    @Override
    public @Range(from = -1, to = 0xFFFF) int next() throws TomlException {
        int c = this.readChar();
        if ((this.flags & BOM_CHECKED) == 0) {
            this.flags |= BOM_CHECKED;
            if (c == 0xFEFF) {
                if ((this.flags & BOM_ALLOWED) == 0) {
                    throw new TomlBomException("Source data contains a BOM");
                } else {
                    this.flags |= BOM_READ;
                    c = this.readChar();
                }
            } else if ((this.flags & BOM_REQUIRED) != 0) {
                throw new TomlBomException("Source data does not contain a BOM");
            }
        }
        return c;
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
