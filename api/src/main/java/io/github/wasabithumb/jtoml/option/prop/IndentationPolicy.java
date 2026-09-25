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
 * Determines how a TOML document should be indented
 * when writing.
 * <h2>Rules</h2>
 * <ul>
 *     <li>
 *         {@link #indentChar()} defines the character used to perform indentation,
 *         either SPACE or TAB.
 *     </li>
 *     <li>
 *         {@link #globalIndent()} defines the initial indentation
 *         for each line.
 *     </li>
 *     <li>
 *         The indentation level is set to {@link #globalIndent()} plus {@link #constantIndent()}
 *         directly before each table declaration, as well as an additional {@link #variableIndent()} for each
 *         additional part in the table path.
 *         For instance, the declaration {@code [a.b.c]} would set the indent level to
 *         {@code globalIndent + constantIndent + 2 * variableIndent}.
 *     </li>
 *     <li>
 *         The indentation level is incremented by {@link #postIndent()} directly after each table
 *         declaration.
 *     </li>
 *     <li>
 *         The indentation level is incremented/decremented by {@link #elementIndent()} before/after
 *         each value in an array.
 *     </li>
 * </ul>
 */
public final class IndentationPolicy {

    /** No indentation */
    public static final IndentationPolicy NONE;

    /**
     * A basic sensible indentation policy
     * with {@code '\t'} (TAB) as the indent character,
     * a {@link #variableIndent() variable indent} of {@code 1}
     * and an {@link #elementIndent() element indent} of {@code 1}.
     */
    public static final IndentationPolicy STANDARD;

    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    static {
        byte[] i0 = new byte[Kind.MAX];
        byte[] i1 = new byte[Kind.MAX];
        i1[Kind.VARIABLE] = 1;
        i1[Kind.ELEMENT] = 1;
        NONE = new IndentationPolicy('\0', i0);
        STANDARD = new IndentationPolicy('\t', i1);
    }

    //

    private final char indentChar;
    private final byte[] indentation;

    private IndentationPolicy(char indentChar, byte[] indentation) {
        this.indentChar = indentChar;
        this.indentation = indentation;
    }

    //

    private @Range(from = 0, to = 255) int indentation(@Kind int kind) {
        return Byte.toUnsignedInt(this.indentation[kind]);
    }

    public char indentChar() {
        return this.indentChar;
    }

    public @Range(from=0, to=255) int globalIndent() {
        return this.indentation(Kind.GLOBAL);
    }

    public @Range(from=0, to=255) int constantIndent() {
        return this.indentation(Kind.CONSTANT);
    }

    public @Range(from=0, to=255) int variableIndent() {
        return this.indentation(Kind.VARIABLE);
    }

    public @Range(from=0, to=255) int postIndent() {
        return this.indentation(Kind.POST);
    }

    public @Range(from=0, to=255) int elementIndent() {
        return this.indentation(Kind.ELEMENT);
    }

    @Override
    public int hashCode() {
        int h = 7;
        h = 31 * h + (int) this.indentChar;
        for (int i = 0; i < Kind.MAX; i++)
            h = 31 * h + (int) this.indentation[i];
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IndentationPolicy)) return false;
        IndentationPolicy other = (IndentationPolicy) obj;
        if (this.indentChar != other.indentChar) return false;
        return Arrays.equals(this.indentation, other.indentation);
    }

    @Override
    public @NotNull String toString() {
        int c = this.indentChar;
        return "IndentPolicy[indentChar=0x" +
                Character.forDigit(c >> 4, 16) +
                Character.forDigit(c & 0xF, 16) +
                ", globalIndent=" +
                this.globalIndent() +
                ", constantIndent=" +
                this.constantIndent() +
                ", variableIndent=" +
                this.variableIndent() +
                ", postIndent=" +
                this.postIndent() +
                ", elementIndent=" +
                this.elementIndent() +
                "]";
    }

    //

    public static final class Builder {

        private char indentChar    = '\t';
        private final byte[] indentation = new byte[Kind.MAX];

        //

        @Contract("_ -> this")
        public @NotNull Builder indentChar(
                @MagicConstant(intValues = { ' ', '\t' }) char indentChar
        ) {
            if (indentChar != ' ' && indentChar != '\t')
                throw new IllegalArgumentException("Invalid indent character (expected TAB or SPACE)");

            this.indentChar = indentChar;
            return this;
        }

        @Contract("_, _ -> this")
        private @NotNull Builder indentation(@Kind int kind, int value) {
            if (value < 0) throw new IllegalArgumentException("Indentation level may not be negative");
            if (value > 255) throw new IllegalArgumentException("Indentation level is too large (" + value + " > 255)");
            this.indentation[kind] = (byte) value;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder globalIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.GLOBAL, indent);
        }

        @Contract("_ -> this")
        public @NotNull Builder constantIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.CONSTANT, indent);
        }

        @Contract("_ -> this")
        public @NotNull Builder variableIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.VARIABLE, indent);
        }

        @Contract("_ -> this")
        public @NotNull Builder postIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.POST, indent);
        }

        @Contract("_ -> this")
        public @NotNull Builder elementIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.ELEMENT, indent);
        }

        @Contract("_ -> this")
        public @NotNull Builder scale(int amount) {
            if (amount < 0) throw new IllegalArgumentException("Scale may not be negative");

            int cur;
            int product;
            for (int i = 0; i < Kind.MAX; i++) {
                cur = Byte.toUnsignedInt(this.indentation[i]);
                try {
                    product = Math.multiplyExact(cur, amount);
                    if (product > 255) product = -1;
                } catch (ArithmeticException e) {
                    product = -1;
                }
                if (product == -1) {
                    throw new IllegalArgumentException("Scale is too large (" + cur + " * " + amount + " > 255)");
                }
                this.indentation[i] = (byte) product;
            }

            return this;
        }

        //

        @Contract("-> new")
        public @NotNull IndentationPolicy build() {
            return new IndentationPolicy(
                    this.indentChar,
                    Arrays.copyOf(this.indentation, Kind.MAX)
            );
        }

    }

    /**
     * Indices in the {@link IndentationPolicy}'s
     * internal array for each kind of indentation
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
    @MagicConstant(valuesFromClass = Kind.class)
    private @interface Kind {
        int GLOBAL = 0;
        int CONSTANT = 1;
        int VARIABLE = 2;
        int POST = 3;
        int ELEMENT = 4;
        int MAX = 5;
    }

}
