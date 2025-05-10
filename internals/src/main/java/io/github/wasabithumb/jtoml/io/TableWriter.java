package io.github.wasabithumb.jtoml.io;

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.io.target.CharTarget;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.option.prop.IndentationPolicy;
import io.github.wasabithumb.jtoml.option.prop.LineSeparator;
import io.github.wasabithumb.jtoml.option.prop.PaddingPolicy;
import io.github.wasabithumb.jtoml.option.prop.SpacingPolicy;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.Set;

public final class TableWriter implements Closeable {

    private final CharTarget out;
    private final JTomlOptions options;
    private int indentLevel = 0;

    public TableWriter(@NotNull CharTarget out, @NotNull JTomlOptions options) {
        this.out = out;
        this.options = options;
    }

    //

    public void writeTable(@NotNull TomlTable table) throws TomlException {
        this.indentLevel = this.options.get(JTomlOption.INDENTATION).globalIndent();
        this.writeTableBody(TomlKey.literal(), table);
    }

    private void writeIndent() {
        final char c = this.options.get(JTomlOption.INDENTATION).indentChar();
        for (int i=0; i < this.indentLevel; i++) this.out.put(c);
    }

    private void writeTableHeader(@NotNull TomlKey key, boolean array) throws TomlException {
        final SpacingPolicy spacing         = this.options.get(JTomlOption.SPACING);
        final PaddingPolicy padding         = this.options.get(JTomlOption.PADDING);
        final IndentationPolicy indentation = this.options.get(JTomlOption.INDENTATION);
        final LineSeparator newline         = this.options.get(JTomlOption.LINE_SEPARATOR);

        this.indentLevel = indentation.globalIndent() + indentation.constantIndent();
        int ks = key.size();
        if (ks > 1) this.indentLevel += (ks * indentation.variableIndent());

        for (int i=0; i < spacing.preTable(); i++) this.out.put(newline);
        this.writeIndent();
        this.out.put('[');
        if (array) {
            for (int i=0; i < padding.tablePadding(); i++) this.out.put(' ');
            this.out.put('[');
        }
        for (int i=0; i < padding.tablePadding(); i++) this.out.put(' ');
        this.out.put(key.toString());
        if (array) {
            for (int i=0; i < padding.tablePadding(); i++) this.out.put(' ');
            this.out.put(']');
        }
        for (int i=0; i < padding.tablePadding(); i++) this.out.put(' ');
        this.out.put(']');
        for (int i=0; i < (spacing.postTable() + 1); i++) this.out.put(newline);

        this.indentLevel += indentation.postIndent();
    }

    private void writeTableHeader(@NotNull TomlKey key) throws TomlException {
        this.writeTableHeader(key, false);
    }

    private void writeTableBody(@NotNull TomlKey prefix, @NotNull TomlTable table) throws TomlException {
        Set<TomlKey> set = table.keys(false);
        int count = set.size();

        // Bin 0: Primitives
        // Bin 1: Arrays
        // Bin 2: Arrays of Tables
        // Bin 3: Tables
        KeyBin b0 = new KeyBin(count);
        KeyBin b1 = new KeyBin(count);
        KeyBin b2 = new KeyBin(count);
        KeyBin b3 = new KeyBin(count);
        for (TomlKey tk : set) {
            TomlValue tv = table.get(tk);
            assert tv != null;
            if (tv.isTable()) {
                b3.add(tk);
            } else if (tv.isArray()) {
                if (this.isArrayOfTables(tv.asArray())) {
                    b2.add(tk);
                } else {
                    b1.add(tk);
                }
            } else {
                b0.add(tk);
            }
        }

        TomlKey nextKey;
        TomlValue nextValue;

        for (int i=0; i < b0.size(); i++) {
            nextKey = b0.get(i);
            nextValue = table.get(nextKey);
            assert nextValue != null;
            this.writePrimitive(nextKey, nextValue.asPrimitive());
        }

        for (int i=0; i < b1.size(); i++) {
            nextKey = b1.get(i);
            nextValue = table.get(nextKey);
            assert nextValue != null;
            this.writeArray(nextKey, nextValue.asArray());
        }

        for (int i=0; i < b2.size(); i++) {
            nextKey = b2.get(i);
            nextValue = table.get(nextKey);
            assert nextValue != null;
            TomlArray arr = nextValue.asArray();
            TomlTable child;
            for (int z=0; z < arr.size(); z++) {
                child = arr.get(z).asTable();
                this.writeTableHeader(nextKey, true);
                this.writeTableBody(nextKey, child);
            }
        }

        for (int i=0; i < b3.size(); i++) {
            nextKey = b3.get(i);
            nextValue = table.get(nextKey);
            assert nextValue != null;
            nextKey = TomlKey.join(prefix, nextKey);
            this.writeTableHeader(nextKey);
            this.writeTableBody(nextKey, nextValue.asTable());
        }
    }

    private void openStatement(@NotNull TomlKey key) throws TomlException {
        final SpacingPolicy spacing = this.options.get(JTomlOption.SPACING);
        final LineSeparator newline = this.options.get(JTomlOption.LINE_SEPARATOR);
        for (int i=0; i < spacing.preStatement(); i++) this.out.put(newline);
        this.writeIndent();
        this.out.put(key.toString());
        this.out.put(" = ");
    }

    private void closeStatement() throws TomlException {
        final SpacingPolicy spacing = this.options.get(JTomlOption.SPACING);
        final LineSeparator newline = this.options.get(JTomlOption.LINE_SEPARATOR);
        for (int i=0; i < (spacing.postStatement() + 1); i++) this.out.put(newline);
    }

    private void writePrimitive(@NotNull TomlKey key, @NotNull TomlPrimitive value) throws TomlException {
        this.openStatement(key);
        this.writePrimitiveValue(value);
        this.closeStatement();
    }

    private void writePrimitiveValue(@NotNull TomlPrimitive value) throws TomlException {
        if (value.isString()) {
            this.writeBasicString(value.asString());
        } else {
            this.out.put(value.asString());
        }
    }

    private void writeBasicString(@NotNull String s) throws TomlException {
        this.out.put('"');

        int c;
        for (int i=0; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == '"') {
                this.out.put("\\\"");
            } else if (c == '\\') {
                this.out.put("\\\\");
            } else if ((' ' <= c && c <= 0x007E) || (0x0080 <= c && c < 0xFFF0)) {
                this.out.put(c);
            } else if (c == '\b') {
                this.out.put("\\b");
            } else if (c == '\f') {
                this.out.put("\\f");
            } else if (c == '\n') {
                this.out.put("\\n");
            } else if (c == '\r') {
                this.out.put("\\r");
            } else if (c == '\t') {
                this.out.put("\\t");
            } else {
                this.out.put("\\u");
                this.out.put(Character.forDigit((c >> 12) & 0xFF, 16));
                this.out.put(Character.forDigit((c >> 8) & 0xFF, 16));
                this.out.put(Character.forDigit((c >> 4) & 0xFF, 16));
                this.out.put(Character.forDigit(c & 0xFF, 16));
            }
        }

        this.out.put('"');
    }

    private void writeArray(@NotNull TomlKey key, @NotNull TomlArray value) throws TomlException {
        this.openStatement(key);
        this.writeArrayValue(value);
        this.closeStatement();
    }

    private void writeArrayValue(@NotNull TomlArray value) throws TomlException {
        final PaddingPolicy padding = this.options.get(JTomlOption.PADDING);
        this.out.put('[');
        for (int i=0; i < padding.arrayPadding(); i++) this.out.put(' ');

        TomlValue next;
        for (int i=0; i < value.size(); i++) {
            if (i != 0) {
                this.out.put(',');
                for (int z=0; z < padding.elementPadding(); z++) this.out.put(' ');
            }
            next = value.get(i);
            this.writeAnyValue(next);
        }

        for (int i=0; i < padding.arrayPadding(); i++) this.out.put(' ');
        this.out.put(']');
    }

    private void writeInlineTableValue(@NotNull TomlTable table) throws TomlException {
        final PaddingPolicy padding = this.options.get(JTomlOption.PADDING);
        this.out.put('{');
        for (int i=0; i < padding.inlineTablePadding(); i++) this.out.put(' ');

        boolean first = true;
        TomlValue next;
        for (TomlKey key : table.keys(false)) {
            if (!first) {
                this.out.put(',');
                for (int z=0; z < padding.elementPadding(); z++) this.out.put(' ');
            }
            first = false;
            next = table.get(key);
            assert next != null;
            this.out.put(key.toString());
            this.out.put(" = ");
            this.writeAnyValue(next);
        }

        for (int i=0; i < padding.inlineTablePadding(); i++) this.out.put(' ');
        this.out.put('}');
    }

    private void writeAnyValue(@NotNull TomlValue value) throws TomlException {
        if (value.isPrimitive()) {
            this.writePrimitiveValue(value.asPrimitive());
        } else if (value.isArray()) {
            this.writeArrayValue(value.asArray());
        } else {
            this.writeInlineTableValue(value.asTable());
        }
    }

    private boolean isArrayOfTables(@NotNull TomlArray array) throws TomlException {
        final int size = array.size();
        if (size == 0) {
            // Subjective decision: x = [] looks better than [[x]]
            return false;
        }

        for (int i=0; i < size; i++) {
            if (!array.get(i).isTable()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void close() throws TomlException {
        this.out.close();
    }

    //

    private static final class KeyBin {

        private final TomlKey[] arr;
        private int head;

        KeyBin(int capacity) {
            this.arr = new TomlKey[capacity];
            this.head = 0;
        }

        //

        public int size() {
            return this.head;
        }

        public void add(@NotNull TomlKey key) {
            int idx = 0;
            int cmp;
            while (idx < this.head) {
                cmp = key.compareTo(this.arr[idx]);
                if (cmp < 0) {
                    break;
                } else if (cmp == 0) {
                    return;
                } else {
                    idx++;
                }
            }
            System.arraycopy(this.arr, idx, this.arr, idx + 1, this.head - idx);
            this.arr[idx] = key;
            this.head++;
        }

        public @NotNull TomlKey get(int index) {
            return this.arr[index];
        }

    }

}
