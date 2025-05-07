package io.github.wasabithumb.jtoml.option.prop;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

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
 * </ul>
 */
public final class IndentationPolicy {

    /** No indentation */
    public static final IndentationPolicy NONE = new IndentationPolicy('\0', 0x00000000);

    /**
     * indentationLevel is initialized to 0.
     * Table headers set indentationLevel to {@code key.size() - 1}.
     * Each line is preceded by "indentationLevel" TAB characters.
     */
    public static final IndentationPolicy STANDARD = new IndentationPolicy('\t', 0x00000100);

    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    //

    private final char indentChar;
    private final int data;

    private IndentationPolicy(char indentChar, int data) {
        this.indentChar = indentChar;
        this.data = data;
    }

    //

    public char indentChar() {
        return this.indentChar;
    }

    public @Range(from=0, to=255) int globalIndent() {
        return (this.data >> 24) & 0xFF;
    }

    public @Range(from=0, to=255) int constantIndent() {
        return (this.data >> 16) & 0xFF;
    }

    public @Range(from=0, to=255) int variableIndent() {
        return (this.data >> 8) & 0xFF;
    }

    public @Range(from=0, to=255) int postIndent() {
        return this.data & 0xFF;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.data);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IndentationPolicy)) return false;
        IndentationPolicy other = (IndentationPolicy) obj;
        if (this.indentChar != other.indentChar) return false;
        return this.data == other.data;
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
                "]";
    }

    //

    public static final class Builder {

        private final int[] values = new int[] { 0, 0, 0, 0 };
        private char indentChar    = '\t';

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
        private @NotNull Builder setValue(int index, int value) {
            if (value < 0) throw new IllegalArgumentException("Indentation level may not be negative");
            if (value > 255) throw new IllegalArgumentException("Indentation level is too large (" + value + " > 255)");
            this.values[index] = value;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder globalIndent(@Range(from=0, to=255) int indent) {
            return this.setValue(0, indent);
        }

        @Contract("_ -> this")
        public @NotNull Builder constantIndent(@Range(from=0, to=255) int indent) {
            return this.setValue(1, indent);
        }

        @Contract("_ -> this")
        public @NotNull Builder variableIndent(@Range(from=0, to=255) int indent) {
            return this.setValue(2, indent);
        }

        @Contract("_ -> this")
        public @NotNull Builder postIndent(@Range(from=0, to=255) int indent) {
            return this.setValue(3, indent);
        }

        @Contract("_ -> this")
        public @NotNull Builder scale(int amount) {
            if (amount < 0) throw new IllegalArgumentException("Scale may not be negative");

            int cur;
            int product;
            for (int i=0; i < 4; i++) {
                cur = this.values[i];
                try {
                    product = Math.multiplyExact(cur, amount);
                    if (product > 255) product = -1;
                } catch (ArithmeticException e) {
                    product = -1;
                }
                if (product == -1) {
                    throw new IllegalArgumentException("Scale is too large (" + cur + " * " + amount + " > 255)");
                }
                this.values[i] = product;
            }

            return this;
        }

        //

        @Contract("-> new")
        public @NotNull IndentationPolicy build() {
            return new IndentationPolicy(
                    this.indentChar,
                    (this.values[0] << 24) | (this.values[1] << 16) |
                            (this.values[2] << 8) | (this.values[3])
            );
        }

    }

}
