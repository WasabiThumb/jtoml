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

package io.github.wasabithumb.jtoml.option.prop;

import org.jetbrains.annotations.Contract;

/**
 * Value of the {@link io.github.wasabithumb.jtoml.option.JTomlOption#LINE_SEPARATOR LINE_SEPARATOR}
 * option. Determines the newline sequence to use between lines
 * of written documents. Does not change which newline sequences
 * are accepted by the parser. Must be one of {@link #LF} or {@link #CRLF}
 * per the spec. {@link #SYSTEM} is also provided as a utility.
 */
public enum LineSeparator implements CharSequence {
    /**
     * UNIX newline ({@code \n}, {@code 0x0A})
     */
    LF(new char[] { '\n' }),

    /**
     * Windows newline ({@code \r\n}, {@code 0x0D 0x0A})
     */
    CRLF(new char[] { '\r', '\n' });

    //

    /**
     * The system line separator, determined by
     * {@link System#lineSeparator()}. This is
     * either {@link #LF} or {@link #CRLF} exactly,
     * not a separate third constant.
     */
    public static final LineSeparator SYSTEM;
    static {
        SYSTEM = System.lineSeparator().charAt(0) == '\r' ?
                CRLF : LF;
    }

    //

    private final char[] data;

    LineSeparator(char [] data) {
        this.data = data;
    }

    //

    @Override
    public int length() {
        return this.data.length;
    }

    @Override
    public char charAt(int index) {
        return this.data[index];
    }

    @Override
    @Contract("_, _ -> fail")
    public CharSequence subSequence(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return new String(this.data);
    }

}
