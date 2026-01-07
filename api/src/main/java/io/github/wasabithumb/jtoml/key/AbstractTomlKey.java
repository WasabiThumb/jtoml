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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.StringJoiner;
import java.util.stream.Stream;

@ApiStatus.Internal
abstract class AbstractTomlKey extends AbstractCollection<String> implements TomlKey {

    protected static boolean isValidBare(char c) {
        if ('A' <= c && c <= 'Z') return true;
        if ('a' <= c && c <= 'z') return true;
        if ('0' <= c && c <= '9') return true;
        return (c == '_' || c == '-');
    }

    protected static boolean isValidBasicUnescaped(char c) {
        if (c < ' ') return c == '\t';
        return (c != '"') && (c != '\\') && (c != (char) 0x7F);
    }

    protected static @NotNull String basicEscape(char c) {
        switch (c) {
            case '\b':
                return "\\b";
            case '\t':
                return "\\t";
            case '\n':
                return "\\n";
            case '\f':
                return "\\f";
            case '\r':
                return "\\r";
            case '"':
                return "\\\"";
            case '\\':
                return "\\\\";
            default:
                return new String(new char[] {
                        '\\', 'u',
                        Character.forDigit((c >> 12) & 0xF, 16),
                        Character.forDigit((c >>  8) & 0xF, 16),
                        Character.forDigit((c >>  4) & 0xF, 16),
                        Character.forDigit(c         & 0xF, 16)
                });
        }
    }

    //

    @Override
    public abstract @NotNull Stream<String> stream();

    @Override
    public @NotNull Iterator<String> iterator() {
        return this.stream().iterator();
    }

    @Override
    public int hashCode() {
        int h = 0;
        Iterator<String> iter = this.iterator();
        if (iter.hasNext()) {
            String next;
            while (true) {
                next = iter.next();
                for (int i = 0; i < next.length(); i++) {
                    h = 31 * h + ((int) next.charAt(i));
                }
                boolean cont = iter.hasNext();
                if (!cont) break;
                h = 31 * h + ((int) '.');
            }
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TomlKey)) return false;

        TomlKey other = (TomlKey) obj;
        int len = this.size();
        if (len != other.size()) return false;

        Iterator<String> a = this.iterator();
        Iterator<String> b = other.iterator();
        for (int i=0; i < len; i++) {
            if (!a.next().equals(b.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull String toString() {
        StringJoiner sj = new StringJoiner(".");
        for (String part : this) sj.add(this.encodePart(part));
        return sj.toString();
    }

    protected @NotNull CharSequence encodePart(@NotNull CharSequence part) {
        final int len = part.length();
        if (len == 0) return "\"\"";

        StringBuilder sb = null;
        boolean encoding = false;
        char c;

        for (int i=0; i < len; i++) {
            c = part.charAt(i);
            if (!encoding) {
                if (isValidBare(c)) continue;
                encoding = true;
                sb = new StringBuilder((len * 5) / 3);
                sb.append('\"').append(part.subSequence(0, i));
            }
            if (isValidBasicUnescaped(c)) {
                sb.append(c);
            } else {
                sb.append(basicEscape(c));
            }
        }

        if (encoding) {
            return sb.append('\"');
        } else {
            return part;
        }
    }

}
