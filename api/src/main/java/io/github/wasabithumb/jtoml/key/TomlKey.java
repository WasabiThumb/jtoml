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

package io.github.wasabithumb.jtoml.key;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a parsed TOML key by parts.
 * {@link TomlKey}s are immutable, so JCF
 * methods which would mutate the key
 * such as {@link List#add(Object) #add}
 * will instead raise an {@link UnsupportedOperationException}.
 * Unlike a normal immutable {@link List}, the
 * {@link #toString()} method formats
 * the key into TOML such that it would
 * be valid to include as-is in a document,
 * complete with escaping and separators.
 * By virtue of implementing {@link Comparable},
 * keys can be compared lexicographically. This
 * is the same ordering used by
 * {@link io.github.wasabithumb.jtoml.value.table.TomlTable TomlTable}.
 * Implementing this interface outside JToml is
 * highly error-prone and not supported.
 * @see #parse(CharSequence)
 */
@ApiStatus.NonExtendable
public interface TomlKey extends List<String>, Comparable<TomlKey> {

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
    @Contract("_ -> new")
    static <S extends CharSequence> @NotNull TomlKey literal(final @NotNull S @NotNull ... parts) {
        final int len = parts.length;
        String[] cpy = new String[len];
        for (int i = 0; i < len; i++) {
            cpy[i] = parts[i].toString();
        }
        return new ArrayTomlKey(cpy);
    }

    /**
     * Wraps a pre-parsed TOML key (by parts) into a {@link TomlKey} object.
     * No parsing is performed.
     * @see #parse(CharSequence)
     */
    @Contract("_ -> new")
    static @NotNull TomlKey literal(@NotNull Collection<? extends CharSequence> parts) {
        final int len = parts.size();
        String[] cpy = new String[len];

        Iterator<? extends CharSequence> iter = parts.iterator();
        for (int i = 0; i < len; i++) {
            if (!iter.hasNext()) throw new ConcurrentModificationException();
            cpy[i] = iter.next().toString();
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
     * @throws IndexOutOfBoundsException Index is less than 0 or not less than {@link #size()}.
     */
    @Override
    @NotNull String get(int index) throws IndexOutOfBoundsException;

    /**
     * Produces a potentially new
     * {@link TomlKey} which represents
     * a section of this key.
     * @param fromIndex The index of the first part to include.
     * @param toIndex The index of the first part to exclude.
     * @throws IllegalArgumentException Negative or out of bounds range
     */
    default @NotNull TomlKey slice(int fromIndex, int toIndex) {
        if (fromIndex == 0 && toIndex == this.size()) return this;
        return SlicedTomlKey.of(this, fromIndex, toIndex - fromIndex);
    }

    /**
     * Alias for {@link #slice(int, int)}.
     */
    @Override
    default @NotNull TomlKey subList(int fromIndex, int toIndex) {
        return this.slice(fromIndex, toIndex);
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
    @Override
    @NotNull String toString();

    /**
     * Performs lexicographical comparison
     * between TOML keys.
     * @param o Another TOML key.
     * @return 0 if the keys are equal,
     *         less than 0 if this key is less than the given key,
     *         greater than 0 if this key is greater than the given key
     */
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
