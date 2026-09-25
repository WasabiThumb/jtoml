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

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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
 */
public final class SpacingPolicy {

    /** No spacing */
    public static final SpacingPolicy NONE;

    /** Places 1 newline before each header, no spacing otherwise */
    public static final SpacingPolicy STANDARD;

    @Contract("-> new")
    public static @NotNull Builder builder() {
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

    public @Range(from=0, to=255) int preHeader() {
        return this.get(Kind.PRE_HEADER);
    }

    public @Range(from=0, to=255) int postHeader() {
        return this.get(Kind.POST_HEADER);
    }

    public @Range(from=0, to=255) int preStatement() {
        return this.get(Kind.PRE_STATEMENT);
    }

    public @Range(from=0, to=255) int postStatement() {
        return this.get(Kind.POST_STATEMENT);
    }

    public @Range(from = 0, to = 255) int postBlock() {
        return this.get(Kind.POST_BLOCK);
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
    public @NotNull String toString() {
        return "SpacingPolicy[preTable=" + this.preTable() +
                ", postTable=" + this.postTable() +
                ", preStatement=" + this.preStatement() +
                ", postStatement=" + this.postStatement() +
                ", postBlock=" + this.postBlock() +
                "]";
    }

    //

    public static final class Builder {

        private final byte[] data = new byte[Kind.MAX];

        //

        @Contract("_, _ -> this")
        private @NotNull Builder set(@Kind int kind, int value) {
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
        public @NotNull Builder preTable(@Range(from = 0, to = 255) int spacing) {
            return this.preHeader(spacing);
        }

        /**
         * @deprecated Use {@link #postHeader(int) postHeader} instead.
         */
        @Deprecated
        @Contract("_ -> this")
        public @NotNull Builder postTable(@Range(from=0, to=255) int spacing) {
            return this.postHeader(spacing);
        }

        @Contract("_ -> this")
        public @NotNull Builder preHeader(@Range(from = 0, to = 255) int spacing) {
            return this.set(Kind.PRE_HEADER, spacing);
        }

        @Contract("_ -> this")
        public @NotNull Builder postHeader(@Range(from=0, to=255) int spacing) {
            return this.set(Kind.POST_HEADER, spacing);
        }

        @Contract("_ -> this")
        public @NotNull Builder preStatement(@Range(from=0, to=255) int spacing) {
            return this.set(Kind.PRE_STATEMENT, spacing);
        }

        @Contract("_ -> this")
        public @NotNull Builder postStatement(@Range(from=0, to=255) int spacing) {
            return this.set(Kind.POST_STATEMENT, spacing);
        }

        @Contract("_ -> this")
        public @NotNull Builder postBlock(@Range(from=0, to=255) int spacing) {
            return this.set(Kind.POST_BLOCK, spacing);
        }

        @Contract("-> new")
        public @NotNull SpacingPolicy build() {
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
