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

package io.github.wasabithumb.jtoml.key;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.RandomAccess;
import java.util.stream.Stream;

@ApiStatus.Internal
final class ArrayTomlKey extends AbstractTomlKey implements RandomAccess {

    public static @NotNull ArrayTomlKey parse(@NotNull CharSequence str) throws IllegalArgumentException {
        if (str instanceof String) {
            str = CharBuffer.wrap(str);
        }

        int count = 0;
        LinkedInt linkedDots = null;
        char quot = '\0';
        boolean escaped = false;
        char c;

        for (int i=0; i < str.length(); i++) {
            c = str.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (quot != '\0') {
                if (quot == '"' && c == '\\') {
                    escaped = true;
                } else if (c == quot) {
                    quot = '\0';
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                if (i != 0 && str.charAt(i - 1) != '.') {
                    throw new IllegalArgumentException("Disallowed quote char at index " + i);
                }
                quot = c;
                continue;
            }
            if (c != '.') continue;
            linkedDots = new LinkedInt(i, linkedDots);
            count++;
        }

        if (count == 0) {
            return new ArrayTomlKey(new String[]{ parseSingle(0, str) });
        }

        String[] parts = new String[count + 1];
        int b = str.length();
        for (int i=count; i > 0; i--) {
            parts[i] = parseSingle(i, str.subSequence(linkedDots.value + 1, b));
            b = linkedDots.value;
            linkedDots = linkedDots.next;
        }
        parts[0] = parseSingle(0, str.subSequence(0, b));

        return new ArrayTomlKey(parts);
    }

    private static @NotNull String parseSingle(int index, @NotNull CharSequence str) throws IllegalArgumentException {
        final int len = str.length();
        if (len == 0) throw new IllegalArgumentException("Part #" + index + " is empty");

        char c0 = str.charAt(0);
        if (c0 == '"') {
            if (len == 1 || str.charAt(len - 1) != '"') {
                throw new IllegalArgumentException("Part #" + index + " is missing closing quotation mark");
            }

            StringBuilder sb = null;
            boolean decoding = false;
            char ci;
            for (int i=1; i < (len - 1); i++) {
                ci = str.charAt(i);
                if (isValidBasicUnescaped(ci)) {
                    if (decoding) sb.append(ci);
                    continue;
                }
                if (ci != '\\') {
                    throw new IllegalArgumentException("Part #" + index + " has illegal char at index " + i);
                }
                if (!decoding) {
                    decoding = true;
                    sb = new StringBuilder(len);
                    sb.append(str.subSequence(1, i));
                }
                int skip = decodeEscape(sb, str.subSequence(i + 1, len - 1));
                if (skip == -1) {
                    throw new IllegalArgumentException("Part #" + index + " has illegal escape sequence at index " + i);
                }
                i += skip;
            }

            if (decoding) {
                return sb.toString();
            } else {
                return str.subSequence(1, len - 1)
                        .toString();
            }
        } else if (c0 == '\'') {
            if (len == 1 || str.charAt(len - 1) != '\'') {
                throw new IllegalArgumentException("Part #" + index + " is missing closing single quote");
            }

            for (int i=1; i < (len - 1); i++) {
                if (str.charAt(i) != '\'') continue;
                throw new IllegalArgumentException("Part #" + index + " has illegal char at index " + i);
            }

            return str.subSequence(1, len - 1)
                    .toString();
        } else {
            for (int i=0; i < len; i++) {
                if (isValidBare(str.charAt(i))) continue;
                throw new IllegalArgumentException("Part #" + index + " has illegal char at index " + i);
            }
            return str.toString();
        }
    }

    @SuppressWarnings("fallthrough")
    private static @Range(from=-1, to=Integer.MAX_VALUE) int decodeEscape(
            @NotNull StringBuilder dest,
            @NotNull CharSequence str
    ) {
        final int len = str.length();
        if (len == 0) return -1;
        int ul = 5;
        char c0 = str.charAt(0);

        switch (c0) {
            case 'b':
                dest.append('\b');
                return 1;
            case 't':
                dest.append('\t');
                return 1;
            case 'n':
                dest.append('\n');
                return 1;
            case 'f':
                dest.append('\f');
                return 1;
            case 'r':
                dest.append('\r');
                return 1;
            case '"':
                dest.append('"');
                return 1;
            case '\\':
                dest.append("\\");
                return 1;
            case 'U':
                ul = 9;
            case 'u':
                if (len < ul) return -1;
                int value = 0;
                int digit;
                for (int i=1; i < ul; i++) {
                    digit = Character.digit(str.charAt(i), 16);
                    if (digit == -1) return -1;
                    value = (value << 4) | digit;
                }
                if (ul == 9) {
                    dest.append(Character.toChars(value));
                } else {
                    dest.append((char) value);
                }
                return ul;
            default:
                return -1;
        }
    }

    //

    private final String[] data;

    ArrayTomlKey(@NotNull String @NotNull [] data) {
        this.data = data;
    }

    //

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public @NotNull String get(int index) throws IndexOutOfBoundsException {
        return this.data[index];
    }

    @Override
    public @NotNull Stream<String> stream() {
        return Arrays.stream(this.data);
    }

    //

    private static final class LinkedInt {

        final int value;
        final LinkedInt next;

        LinkedInt(int value, LinkedInt next) {
            this.value = value;
            this.next = next;
        }

    }

}
