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

import io.github.wasabithumb.jtoml.util.Buildable;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.annotation.*;
import java.util.Arrays;

/**
 * Determines the amount of space characters to insert
 * within TOML elements.
 * @see #NONE
 * @see #STANDARD
 * @see #builder()
 */
public final class PaddingPolicy implements Buildable<PaddingPolicy> {

    /** No padding */
    public static final PaddingPolicy NONE;

    /** Do not pad table headers; pad inline tables, arrays and their elements by 1 */
    public static final PaddingPolicy STANDARD;

    /**
     * Creates a new {@link Builder} for the purpose
     * of constructing custom {@link PaddingPolicy} instances.
     */
    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    static {
        byte[] d0 = new byte[Kind.MAX];
        byte[] d1 = new byte[Kind.MAX];
        d1[Kind.INLINE_TABLE] = 1;
        d1[Kind.ARRAY] = 1;
        d1[Kind.ELEMENT] = 1;
        NONE = new PaddingPolicy(d0);
        STANDARD = new PaddingPolicy(d1);
    }

    //

    private final byte[] data;

    private PaddingPolicy(byte[] data) {
        this.data = data;
    }

    //

    private @Range(from=0, to=255) int padding(@Kind int kind) {
        return Byte.toUnsignedInt(this.data[kind]);
    }

    /**
     * Reports the number of spaces to insert between
     * the braces and key of a table header,
     * up to 255.
     */
    public @Range(from=0, to=255) int tablePadding() {
        return this.padding(Kind.TABLE);
    }

    /**
     * Reports the number of spaces to insert between
     * the braces and values of an inline table,
     * up to 255.
     */
    public @Range(from=0, to=255) int inlineTablePadding() {
        return this.padding(Kind.INLINE_TABLE);
    }

    /**
     * Reports the number of spaces to insert between
     * the braces and values of an array,
     * up to 255.
     */
    public @Range(from=0, to=255) int arrayPadding() {
        return this.padding(Kind.ARRAY);
    }

    /**
     * Reports the number of spaces to insert between
     * adjacent elements within an array when they
     * are on the same line, up to 255.
     */
    public @Range(from=0, to=255) int elementPadding() {
        return this.padding(Kind.ELEMENT);
    }

    @Override
    public @NotNull Builder toBuilder() {
        Builder ret = new Builder();
        System.arraycopy(this.data, 0, ret.data, 0, Kind.MAX);
        return ret;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PaddingPolicy)) return false;
        return Arrays.equals(this.data, ((PaddingPolicy) obj).data);
    }

    @Override
    public @NotNull String toString() {
        return "PaddingPolicy[tablePadding=" + this.tablePadding() +
                ", inlineTablePadding=" + this.inlineTablePadding() +
                ", arrayPadding=" + this.arrayPadding() +
                ", elementPadding=" + this.elementPadding() +
                "]";
    }

    //

    /**
     * Facilitates the creation
     * of a new {@link PaddingPolicy}.
     */
    public static final class Builder implements Buildable.Builder<PaddingPolicy> {

        private final byte[] data = new byte[Kind.MAX];

        //

        @Contract("_, _ -> this")
        private @NotNull Builder set(@Kind int kind, int value) {
            if (value < 0) throw new IllegalArgumentException("Padding may not be negative");
            if (value > 255) throw new IllegalArgumentException("Padding is too large (" + value + " > 255)");
            this.data[kind] = (byte) value;
            return this;
        }

        /**
         * Sets the {@link PaddingPolicy#tablePadding() tablePadding} of the
         * {@link PaddingPolicy} being built.
         * @param padding The padding level.
         * @throws IllegalArgumentException Padding level is less than 0 or greater than 255
         * @return This builder.
         */
        @Contract("_ -> this")
        public @NotNull Builder tablePadding(@Range(from=0, to=255) int padding) {
            return this.set(Kind.TABLE, padding);
        }

        /**
         * Sets the {@link PaddingPolicy#inlineTablePadding()} inlineTablePadding of the
         * {@link PaddingPolicy} being built.
         * @param padding The padding level.
         * @throws IllegalArgumentException Padding level is less than 0 or greater than 255
         * @return This builder.
         */
        @Contract("_ -> this")
        public @NotNull Builder inlineTablePadding(@Range(from=0, to=255) int padding) {
            return this.set(Kind.INLINE_TABLE, padding);
        }

        /**
         * Sets the {@link PaddingPolicy#arrayPadding() arrayPadding} of the
         * {@link PaddingPolicy} being built.
         * @param padding The padding level.
         * @throws IllegalArgumentException Padding level is less than 0 or greater than 255
         * @return This builder.
         */
        @Contract("_ -> this")
        public @NotNull Builder arrayPadding(@Range(from=0, to=255) int padding) {
            return this.set(Kind.ARRAY, padding);
        }

        /**
         * Sets the {@link PaddingPolicy#elementPadding() elementPadding} of the
         * {@link PaddingPolicy} being built.
         * @param padding The padding level.
         * @throws IllegalArgumentException Padding level is less than 0 or greater than 255
         * @return This builder.
         */
        @Contract("_ -> this")
        public @NotNull Builder elementPadding(@Range(from=0, to=255) int padding) {
            return this.set(Kind.ELEMENT, padding);
        }

        @Override
        @Contract("-> new")
        public @NotNull PaddingPolicy build() {
            return new PaddingPolicy(Arrays.copyOf(this.data, Kind.MAX));
        }

    }

    /**
     * Indices in the {@link PaddingPolicy}'s
     * internal array for each kind of spacing
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
    @MagicConstant(valuesFromClass = PaddingPolicy.Kind.class)
    private @interface Kind {
        int TABLE = 0;
        int INLINE_TABLE = 1;
        int ARRAY = 2;
        int ELEMENT = 3;
        //
        int MAX = 4;
    }

}
