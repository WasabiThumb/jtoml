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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Represents a parsed TOML key by parts
 * @see #parse(CharSequence)
 */
@ApiStatus.NonExtendable
public interface TomlKey extends Collection<String>, Comparable<TomlKey> {

    /**
     * Parses a string representation of a TOML key.
     * Extraneous whitespace is <strong>NOT</strong> allowed.
     * <h4>Example</h4>
     * <pre>{@code
     * TomlKey.parse("'foo.bar'.\"\\U0001F60E\"")
     *      .toString(); // "foo.bar"."😎"
     * }</pre>
     * @throws IllegalArgumentException Key is empty, has empty parts, has an illegally placed opening quotation mark/single
     *                                  quote, has a missing closing quotation mark/single quote,
     *                                  has an unescaped special character between opening and closing quotation marks,
     *                                  or has a single quote between opening and closing single quotes
     */
    @Contract("_ -> new")
    static @NotNull TomlKey parse(@NotNull CharSequence key) throws IllegalArgumentException {
        try {
            return ArrayTomlKey.parse(key);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse TOML key (" + key + ")", e);
        }
    }

    /**
     * Returns a TOML key that represents a concatenation of the parts
     * of each provided key. If only 1 key is provided ({@code additional} is empty),
     * the first key ({@code first}) is always returned as-is. Otherwise, a new
     * key is created.
     */
    static @NotNull TomlKey join(@NotNull TomlKey first, @NotNull TomlKey @NotNull ... additional) {
        return JoinedTomlKey.join(first, additional);
    }

    /**
     * Wraps a pre-parsed TOML key (by parts) into a {@link TomlKey} object.
     * No parsing is performed.
     * @see #parse(CharSequence)
     */
    @SafeVarargs
    @ApiStatus.Experimental
    @Contract("_ -> new")
    static <S extends CharSequence> @NotNull TomlKey literal(final @NotNull S @NotNull ... parts) {
        final int len = parts.length;
        String[] cpy = new String[len];
        for (int i=0; i < len; i++) {
            cpy[i] = parts[i].toString();
        }
        return new ArrayTomlKey(cpy);
    }

    //

    /**
     * Provides the number of parts in this key.
     * A valid key is expected to have at least 1 part.
     * The TomlKey that serializes to {@code ""} would have
     * 1 part with a length of 0.
     */
    @Override
    int size();

    /**
     * Gets the Nth part of this key. Depending on the key implementation,
     * this may be less efficient than {@link #stream()}/{@link #iterator()}.
     */
    default @NotNull String get(int index) throws IndexOutOfBoundsException {
        final int size = this.size();
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + size);

        Iterator<String> iter = this.iterator();
        String ret;
        do {
            if (!iter.hasNext()) throw new ConcurrentModificationException();
            ret = iter.next();
            index--;
        } while (index >= 0);

        return ret;
    }

    @NotNull Stream<String> stream();

    @Contract("_, _ -> new")
    default @NotNull TomlKey slice(int fromIndex, int toIndex) {
        return SlicedTomlKey.of(this, fromIndex, toIndex - fromIndex);
    }

    /**
     * Serializes the key represented by this object
     * into a TOML-compatible form, escaping as necessary.
     * Escaping will always be done with basic (double quote) strings,
     * literal (single quote) strings are never chosen.
     * <h4>Example</h4>
     * <pre>{@code
     * TomlKey.literal("lorem ipsum", "dolor", "\"sit amet\"")
     *      .toString() // "lorem ipsum".dolor."\"sit amet\""
     * }</pre>
     */
    @NotNull String toString();

    @Override
    default int compareTo(@NotNull TomlKey o) {
        int ml = this.size();
        int ol = o.size();
        int sl;
        int lc;

        if (ml < ol) {
            sl = ml;
            lc = -1;
        } else if (ml > ol) {
            sl = ol;
            lc = 1;
        } else {
            sl = ml;
            lc = 0;
        }

        Iterator<String> mi = this.iterator();
        Iterator<String> oi = o.iterator();
        int pc;

        for (int i=0; i < sl; i++) {
            pc = mi.next().compareTo(oi.next());
            if (pc != 0) return pc;
        }

        return lc;
    }

}
