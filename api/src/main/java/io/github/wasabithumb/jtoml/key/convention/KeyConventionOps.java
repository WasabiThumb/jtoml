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

package io.github.wasabithumb.jtoml.key.convention;

import io.github.wasabithumb.jtoml.key.TomlKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@ApiStatus.Internal
final class KeyConventionOps {

    static @NotNull TomlKey identity(@NotNull String string) {
        return TomlKey.literal(string);
    }

    static @NotNull TomlKey lower(@NotNull String string) {
        return TomlKey.literal(string.toLowerCase(Locale.ROOT));
    }

    static @NotNull TomlKey camelToKebab(@NotNull String string) {
        return camelToDelimited(string, '-');
    }

    static @NotNull TomlKey camelToSnake(@NotNull String string) {
        return camelToDelimited(string, '_');
    }

    static @NotNull TomlKey camelToSplit(@NotNull String string) {
        List<String> list = new LinkedList<>();
        camelParse(string, list::add, null);
        return TomlKey.literal(list);
    }

    private static @NotNull TomlKey camelToDelimited(@NotNull String string, char delim) {
        StringBuilder ret = new StringBuilder(string.length());
        camelParse(string, ret::append, () -> ret.append(delim));
        return TomlKey.literal(ret.toString());
    }

    private static void camelParse(
            @NotNull String string,
            @NotNull Consumer<String> acceptPart,
            @Nullable Runnable runBetween
    ) {
        final int len = string.length();
        int f = (runBetween == null) ? 2 : 0;
        char[] buf = new char[len];
        int head = 0;
        char c;

        for (int i = 0; i < len; ) {
            c = string.charAt(i++);
            if ('A' > c || c > 'Z') {
                buf[head++] = c;
                continue;
            }
            if (head != 0) {
                if (f == 1) runBetween.run();
                acceptPart.accept(new String(buf, 0, head));
                f |= 1;
                head = 0;
            }
            buf[head++] = (char) (c + 0x20);
            while (i < len && 'A' <= (c = string.charAt(i)) && c <= 'Z') {
                buf[head++] = (char) (c + 0x20);
                i++;
            }
        }

        if (head != 0) {
            if (f == 1) runBetween.run();
            acceptPart.accept(new String(buf, 0, head));
        }
    }

    //

    private KeyConventionOps() { }

}
