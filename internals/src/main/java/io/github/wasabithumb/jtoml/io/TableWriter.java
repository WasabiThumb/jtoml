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
import java.util.ArrayList;
import java.util.List;
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
        this.writeTableBody(TomlKey.literal(), table, false);
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

    private void writeTableBody(
            @NotNull TomlKey prefix,
            @NotNull TomlTable table,
            boolean andHeader
    ) throws TomlException {
        Set<TomlKey> set = table.keys(false);
        int count = set.size();

        // Bin 0: Primitives
        // Bin 1: Arrays
        // Bin 2: Arrays of Tables
        // Bin 3: Tables
        List<TomlKey> b0 = new ArrayList<>(count);
        List<TomlKey> b1 = new ArrayList<>(count);
        List<TomlKey> b2 = new ArrayList<>(count);
        List<TomlKey> b3 = new ArrayList<>(count);
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

        if (andHeader && (this.options.get(JTomlOption.WRITE_EMPTY_TABLES) || !b0.isEmpty() || !b1.isEmpty())) {
            this.writeTableHeader(prefix, false);
        }

        TomlValue nextValue;

        for (TomlKey nextKey : b0) {
            nextValue = table.get(nextKey);
            assert nextValue != null;
            this.writePrimitive(nextKey, nextValue.asPrimitive());
        }

        for (TomlKey nextKey : b1) {
            nextValue = table.get(nextKey);
            assert nextValue != null;
            this.writeArray(nextKey, nextValue.asArray());
        }

        for (TomlKey nextKey : b2) {
            nextValue = table.get(nextKey);
            assert nextValue != null;
            nextKey = TomlKey.join(prefix, nextKey);
            TomlArray arr = nextValue.asArray();
            TomlTable child;
            for (int z=0; z < arr.size(); z++) {
                child = arr.get(z).asTable();
                this.writeTableHeader(nextKey, true);
                this.writeTableBody(nextKey, child, false);
            }
        }

        for (TomlKey nextKey : b3) {
            nextValue = table.get(nextKey);
            assert nextValue != null;
            nextKey = TomlKey.join(prefix, nextKey);
            this.writeTableBody(nextKey, nextValue.asTable(), true);
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
        } else if (value.isFloat()) {
            String v = value.asString();
            this.out.put(v);

            // Ensure that we aren't writing a float as an integer
            if (v.indexOf('.') == -1 &&
                    v.indexOf('e') == -1 &&
                    v.indexOf('E') == -1 &&
                    !v.contains("inf") &&
                    !v.contains("nan")
            ) {
                this.out.put(".0");
            }
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
                this.out.put(Character.forDigit((c >> 12) & 0xF, 16));
                this.out.put(Character.forDigit((c >> 8) & 0xF, 16));
                this.out.put(Character.forDigit((c >> 4) & 0xF, 16));
                this.out.put(Character.forDigit(c & 0xF, 16));
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

        if (value.size() == 0) {
            this.out.put(']');
            return;
        }

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

        Set<TomlKey> keys = table.keys(false);
        if (keys.isEmpty()) {
            this.out.put('}');
            return;
        }

        for (int i=0; i < padding.inlineTablePadding(); i++) this.out.put(' ');

        boolean first = true;
        TomlValue next;
        for (TomlKey key : keys) {
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

}
