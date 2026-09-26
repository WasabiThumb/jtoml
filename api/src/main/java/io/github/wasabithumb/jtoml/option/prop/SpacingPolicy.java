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
import org.jetbrains.annotations.Range;

import java.lang.annotation.*;
import java.util.Arrays;

/**
 * Determines how newlines should be inserted to separate
 * TOML elements. Reference the example below for how
 * each attribute applies.
 * <pre>{@code
 * # preHeader
 * [table]
 * # postHeader
 * # preStatement
 * x = "foo"
 * # postStatement
 * # preStatement
 * y = "bar"
 * # postStatement
 * # postBlock
 * }</pre>
 * @see #NONE
 * @see #STANDARD
 * @see #builder()
 */
public final class SpacingPolicy implements Buildable<SpacingPolicy> {

    /** No spacing */
    public static final SpacingPolicy NONE;

    /** Places 1 newline before each header, no spacing otherwise */
    public static final SpacingPolicy STANDARD;

    /**
     * Creates a new {@link Builder Builder} for the purpose
     * of creating a custom {@link SpacingPolicy} instance.
     */
    @Contract("-> new")
    public static Builder builder() {
        return new Builder();
    }

    static {
        byte[] b1 = new byte[Kind.MAX];
        byte[] b2 = new byte[Kind.MAX];
        b2[0] = 1;
        NONE = new SpacingPolicy(b1);
        STANDARD = new SpacingPolicy(b2);
    }

    //

    private final byte[] data;

    private SpacingPolicy(byte[] data) {
        this.data = data;
    }

    //

    private @Range(from = 0, to = 255) int get(@Kind int kind) {
        return Byte.toUnsignedInt(this.data[kind]);
    }

    /**
     * @deprecated Deprecated alias for {@link #preHeader()}.
     */
    @Deprecated
    public @Range(from=0, to=255) int preTable() {
        return this.preHeader();
    }

    /**
     * @deprecated Deprecated alias for {@link #postHeader()}.
     */
    @Deprecated
    public @Range(from=0, to=255) int postTable() {
        return this.postHeader();
    }

    /**
     * The number of newlines to insert before each header
     * and its comments, up to 255.
     */
    public @Range(from=0, to=255) int preHeader() {
        return this.get(Kind.PRE_HEADER);
    }

    /**
     * The number of newlines to insert after each header
     * and its comments, up to 255.
     */
    public @Range(from=0, to=255) int postHeader() {
        return this.get(Kind.POST_HEADER);
    }

    /**
     * The number of newlines to insert before each statement (key-values)
     * and its comments, up to 255.
     */
    public @Range(from=0, to=255) int preStatement() {
        return this.get(Kind.PRE_STATEMENT);
    }

    /**
     * The number of newlines to insert after each statement (key-values)
     * and its comments, up to 255.
     */
    public @Range(from=0, to=255) int postStatement() {
        return this.get(Kind.POST_STATEMENT);
    }

    /**
     * The number of newlines to insert after each block
     * (subsequent statements following a header up to and excluding the next header)
     * and its comments, up to 255.
     */
    public @Range(from = 0, to = 255) int postBlock() {
        return this.get(Kind.POST_BLOCK);
    }

    @Override
    @Contract("-> new")
    public Builder toBuilder() {
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
        if (!(obj instanceof SpacingPolicy)) return false;
        return Arrays.equals(this.data, ((SpacingPolicy) obj).data);
    }

    @Override
    public String toString() {
        return "SpacingPolicy[preHeader=" + this.preHeader() +
                ", postHeader=" + this.postHeader() +
                ", preStatement=" + this.preStatement() +
                ", postStatement=" + this.postStatement() +
                ", postBlock=" + this.postBlock() +
                "]";
    }

    //

    /**
     * Facilitates the creation of
     * a new {@link SpacingPolicy}.
     */
    public static final class Builder implements Buildable.Builder<SpacingPolicy> {

        private final byte[] data = new byte[Kind.MAX];

        //

        @Contract("_, _ -> this")
        private Builder set(@Kind int kind, int value) {
            if (value < 0) throw new IllegalArgumentException("Spacing may not be negative");
            if (value > 255) throw new IllegalArgumentException("Spacing is too large (" + value + " > 255)");
            this.data[kind] = (byte) value;
            return this;
        }

        /**
         * @deprecated Use {@link #preHeader(int) preHeader} instead.
         */
        @Deprecated
        @Contract("_ -> this")
        public Builder preTable(@Range(from = 0, to = 255) int spacing) {
            return this.preHeader(spacing);
        }

        /**
         * @deprecated Use {@link #postHeader(int) postHeader} instead.
         */
        @Deprecated
        @Contract("_ -> this")
        public Builder postTable(@Range(from=0, to=255) int spacing) {
            return this.postHeader(spacing);
        }

        /**
         * Sets the {@link SpacingPolicy#preHeader() preHeader} spacing
         * of the {@link SpacingPolicy} being built.
         * @param spacing The spacing to use.
         * @throws IllegalArgumentException Spacing is less than 0 or greater than 255.
         * @return This builder.
         */
        @Contract("_ -> this")
        public Builder preHeader(@Range(from = 0, to = 255) int spacing) {
            return this.set(Kind.PRE_HEADER, spacing);
        }

        /**
         * Sets the {@link SpacingPolicy#postHeader() postHeader} spacing
         * of the {@link SpacingPolicy} being built.
         * @param spacing The spacing to use.
         * @throws IllegalArgumentException Spacing is less than 0 or greater than 255.
         * @return This builder.
         */
        @Contract("_ -> this")
        public Builder postHeader(@Range(from=0, to=255) int spacing) {
            return this.set(Kind.POST_HEADER, spacing);
        }

        /**
         * Sets the {@link SpacingPolicy#preStatement() preStatement} spacing
         * of the {@link SpacingPolicy} being built.
         * @param spacing The spacing to use.
         * @throws IllegalArgumentException Spacing is less than 0 or greater than 255.
         * @return This builder.
         */
        @Contract("_ -> this")
        public Builder preStatement(@Range(from=0, to=255) int spacing) {
            return this.set(Kind.PRE_STATEMENT, spacing);
        }

        /**
         * Sets the {@link SpacingPolicy#postStatement() postStatement} spacing
         * of the {@link SpacingPolicy} being built.
         * @param spacing The spacing to use.
         * @throws IllegalArgumentException Spacing is less than 0 or greater than 255.
         * @return This builder.
         */
        @Contract("_ -> this")
        public Builder postStatement(@Range(from=0, to=255) int spacing) {
            return this.set(Kind.POST_STATEMENT, spacing);
        }

        /**
         * Sets the {@link SpacingPolicy#postBlock() postBlock} spacing
         * of the {@link SpacingPolicy} being built.
         * @param spacing The spacing to use.
         * @throws IllegalArgumentException Spacing is less than 0 or greater than 255.
         * @return This builder.
         */
        @Contract("_ -> this")
        public Builder postBlock(@Range(from=0, to=255) int spacing) {
            return this.set(Kind.POST_BLOCK, spacing);
        }

        @Override
        @Contract("-> new")
        public SpacingPolicy build() {
            return new SpacingPolicy(Arrays.copyOf(this.data, Kind.MAX));
        }

    }

    /**
     * Indices in the {@link SpacingPolicy}'s
     * internal array for each kind of spacing
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
    @MagicConstant(valuesFromClass = Kind.class)
    private @interface Kind {
        int PRE_HEADER = 0;
        int POST_HEADER = 1;
        int PRE_STATEMENT = 2;
        int POST_STATEMENT = 3;
        int POST_BLOCK = 4;
        //
        int MAX = 5;
    }

}
