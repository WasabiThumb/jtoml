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
 * Value of the {@link io.github.wasabithumb.jtoml.option.JTomlOption#INDENTATION INDENTATION} option.
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
 * @see #NONE
 * @see #STANDARD
 * @see #builder()
 */
public final class IndentationPolicy implements Buildable<IndentationPolicy> {

    /** No indentation */
    public static final IndentationPolicy NONE;

    /**
     * A basic sensible indentation policy
     * with {@code '\t'} (TAB) as the indent character,
     * a {@link #variableIndent() variable indent} of {@code 1}
     * and an {@link #elementIndent() element indent} of {@code 1}.
     */
    public static final IndentationPolicy STANDARD;

    /**
     * Creates a new {@link Builder} for
     * producing custom {@link IndentationPolicy} instances.
     */
    @Contract("-> new")
    public static Builder builder() {
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

    /**
     * The character to use for indentation.
     * When indentation is enabled, must be either {@code '\t'} (TAB) or {@code ' '} (space).
     * The {@link #NONE} policy may use {@code '\0'} (NUL) for optimization purposes
     * and to ensure the policy is being respected, logically prohibiting that
     * NUL character from being written into a document.
     */
    public char indentChar() {
        return this.indentChar;
    }

    /**
     * The initial indentation for each line,
     * up to 255.
     */
    public @Range(from=0, to=255) int globalIndent() {
        return this.indentation(Kind.GLOBAL);
    }

    /**
     * The indentation to apply before each header
     * irrespective of how many parts the header
     * key contains, up to 255.
     */
    public @Range(from=0, to=255) int constantIndent() {
        return this.indentation(Kind.CONSTANT);
    }

    /**
     * The indentation to apply before each header,
     * as a factor of how many additional (more than 1)
     * parts the header key contains, up to 255.
     */
    public @Range(from=0, to=255) int variableIndent() {
        return this.indentation(Kind.VARIABLE);
    }

    /**
     * The indentation to apply after each header,
     * up to 255.
     */
    public @Range(from=0, to=255) int postIndent() {
        return this.indentation(Kind.POST);
    }

    /**
     * THe indentation to apply before each array
     * element, up to 255. This only affects output
     * when an array spans multiple lines, a decision
     * based on the content of the document and the
     * {@link ArrayStrategy}.
     */
    public @Range(from=0, to=255) int elementIndent() {
        return this.indentation(Kind.ELEMENT);
    }

    @Override
    @Contract("-> new")
    public Builder toBuilder() {
        Builder ret = new Builder();
        ret.indentChar = this.indentChar;
        System.arraycopy(this.indentation, 0, ret.indentation, 0, Kind.MAX);
        return ret;
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
    public String toString() {
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

    /**
     * Facilitates the creation
     * of an {@link IndentationPolicy}.
     */
    public static final class Builder
            implements Buildable.Builder<IndentationPolicy>
    {

        private char indentChar    = '\t';
        private final byte[] indentation = new byte[Kind.MAX];

        //

        /**
         * Sets the {@link IndentationPolicy#indentChar() indentChar}
         * of the resultant {@link IndentationPolicy}.
         * @param indentChar The indentation character to use.
         * @throws IllegalArgumentException {@code indentChar} is not {@code '\t'} or {@code ' '}.
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder indentChar(
                @MagicConstant(intValues = { ' ', '\t' }) char indentChar
        ) {
            if (indentChar != ' ' && indentChar != '\t')
                throw new IllegalArgumentException("Invalid indent character (expected TAB or SPACE)");

            this.indentChar = indentChar;
            return this;
        }

        @Contract(value = "_, _ -> this", mutates = "this")
        private Builder indentation(@Kind int kind, int value) {
            if (value < 0) throw new IllegalArgumentException("Indentation level may not be negative");
            if (value > 255) throw new IllegalArgumentException("Indentation level is too large (" + value + " > 255)");
            this.indentation[kind] = (byte) value;
            return this;
        }

        /**
         * Sets the {@link IndentationPolicy#globalIndent() globalIndent}
         * of the resultant {@link IndentationPolicy}.
         * @param indent The indent level to use.
         * @throws IllegalArgumentException Indent level is less than 0 or more than 255.
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder globalIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.GLOBAL, indent);
        }

        /**
         * Sets the {@link IndentationPolicy#constantIndent() constantIndent}
         * of the resultant {@link IndentationPolicy}.
         * @param indent The indent level to use.
         * @throws IllegalArgumentException Indent level is less than 0 or more than 255.
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder constantIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.CONSTANT, indent);
        }

        /**
         * Sets the {@link IndentationPolicy#variableIndent() variableIndent}
         * of the resultant {@link IndentationPolicy}.
         * @param indent The indent level to use.
         * @throws IllegalArgumentException Indent level is less than 0 or more than 255.
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder variableIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.VARIABLE, indent);
        }

        /**
         * Sets the {@link IndentationPolicy#postIndent() postIndent}
         * of the resultant {@link IndentationPolicy}.
         * @param indent The indent level to use.
         * @throws IllegalArgumentException Indent level is less than 0 or more than 255.
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder postIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.POST, indent);
        }

        /**
         * Sets the {@link IndentationPolicy#elementIndent() elementIndent}
         * of the resultant {@link IndentationPolicy}.
         * @param indent The indent level to use.
         * @throws IllegalArgumentException Indent level is less than 0 or more than 255.
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder elementIndent(@Range(from=0, to=255) int indent) {
            return this.indentation(Kind.ELEMENT, indent);
        }

        /**
         * Scales all indentation levels retained in this builder by
         * the given amount.
         * @param amount The multiplication factor.
         * @throws IllegalArgumentException Multiplication factor is negative or too large,
         *                                  causing any indentation level to exceed 255.
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder scale(int amount) {
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

        /**
         * Creates a new immutable {@link IndentationPolicy} using
         * the immediate state of this builder. Future modifications to this builder
         * will not affect this resultant object.
         * @return A new {@link IndentationPolicy}.
         */
        @Override
        @Contract("-> new")
        public IndentationPolicy build() {
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
