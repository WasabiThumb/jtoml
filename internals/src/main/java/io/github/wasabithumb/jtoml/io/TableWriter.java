package io.github.wasabithumb.jtoml.io;

import io.github.wasabithumb.jtoml.comment.Comment;
import io.github.wasabithumb.jtoml.comment.CommentPosition;
import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.io.target.CharTarget;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.option.prop.*;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.*;

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

        final Comments comments = table.comments();
        final boolean writeComments = this.options.get(JTomlOption.WRITE_COMMENTS) && comments.count() != 0;
        final LineSeparator newline = this.options.get(JTomlOption.LINE_SEPARATOR);

        if (writeComments) {
            for (Comment c : comments.all()) {
                if (c.position() == CommentPosition.POST) break;
                this.out.put("# ");
                this.out.put(c.content());
                this.out.put(newline);
            }
        }

        this.writeTableBody(
                TomlKey.literal(),
                table,
                false
        );

        if (writeComments) {
            for (Comment c : comments.get(CommentPosition.POST)) {
                this.out.put("# ");
                this.out.put(c.content());
                this.out.put(newline);
            }
        }
    }

    private void writeIndent() {
        final char c = this.options.get(JTomlOption.INDENTATION).indentChar();
        for (int i=0; i < this.indentLevel; i++) this.out.put(c);
    }

    private void writeTableHeader0(@NotNull TomlKey key, boolean array, @Nullable String inlineComment) throws TomlException {
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
        if (inlineComment != null) {
            this.out.put(" # ");
            this.out.put(inlineComment);
        }
        for (int i=0; i < (spacing.postTable() + 1); i++) this.out.put(newline);

        this.indentLevel += indentation.postIndent();
    }

    private void writeTableHeader(
            @NotNull TomlKey key,
            @NotNull TomlTable table,
            boolean array,
            boolean unconditional
    ) throws TomlException {
        final Comments comments = table.comments();
        final boolean writeComments = this.options.get(JTomlOption.WRITE_COMMENTS) && comments.count() != 0;
        final boolean writeEmptyTables = this.options.get(JTomlOption.WRITE_EMPTY_TABLES);
        final LineSeparator newline = this.options.get(JTomlOption.LINE_SEPARATOR);

        if (writeComments) {
            for (Comment c : comments.get(CommentPosition.PRE)) {
                this.out.put("# ");
                this.out.put(c.content());
                this.out.put(newline);
            }
        }
        if (writeComments || writeEmptyTables || unconditional) {
            this.writeTableHeader0(
                    key,
                    array,
                    writeComments ? comments.getInline() : null
            );
        }
        if (writeComments) {
            for (Comment c : comments.get(CommentPosition.POST)) {
                this.out.put("# ");
                this.out.put(c.content());
                this.out.put(newline);
            }
        }
    }

    private void writeTableBody(
            @NotNull TomlKey prefix,
            @NotNull TomlTable table,
            boolean andHeader
    ) throws TomlException {
        List<TypedKey> keys = this.deconstruct(table);

        if (andHeader) {
            boolean unconditional = false;
            for (TypedKey typedKey : keys) {
                if (typedKey.type == ValueType.PRIMITIVE ||
                    typedKey.type == ValueType.ARRAY ||
                    typedKey.type == ValueType.INLINE_TABLE
                ) {
                    unconditional = true;
                    break;
                }
            }
            this.writeTableHeader(prefix, table, false, unconditional);
        }

        for (TypedKey typedKey : keys) {
            TomlKey key = typedKey.key;
            TomlValue value = table.get(key);
            assert value != null;

            switch (typedKey.type) {
                case PRIMITIVE:
                    this.writePrimitive(key, value.asPrimitive());
                    break;
                case ARRAY:
                    this.writeArray(key, value.asArray());
                    break;
                case ARRAY_OF_TABLES:
                    key = TomlKey.join(prefix, key);
                    TomlArray arr = value.asArray();
                    TomlTable child;
                    for (int z=0; z < arr.size(); z++) {
                        child = arr.get(z).asTable();
                        this.writeTableHeader(key, child, true, true);
                        this.writeTableBody(key, child, false);
                    }
                    break;
                case TABLE:
                    key = TomlKey.join(prefix, key);
                    this.writeTableBody(key, value.asTable(), true);
                    break;
                case INLINE_TABLE:
                    this.writeInlineTable(key, value.asTable());
                    break;
            }
        }
    }

    private void openStatement(@NotNull TomlKey key, @NotNull Comments comments) throws TomlException {
        final boolean writeComments = this.options.get(JTomlOption.WRITE_COMMENTS) && comments.count() != 0;
        final SpacingPolicy spacing = this.options.get(JTomlOption.SPACING);
        final LineSeparator newline = this.options.get(JTomlOption.LINE_SEPARATOR);

        for (int i=0; i < spacing.preStatement(); i++) this.out.put(newline);
        this.writeIndent();

        if (writeComments) {
            for (Comment c : comments.get(CommentPosition.PRE)) {
                this.out.put("# ");
                this.out.put(c.content());
                this.out.put(newline);
                this.writeIndent();
            }
        }

        this.out.put(key.toString());
        this.out.put(" = ");
    }

    private void closeStatement(@NotNull Comments comments) throws TomlException {
        final boolean writeComments = this.options.get(JTomlOption.WRITE_COMMENTS);
        final String inline = comments.getInline();
        final SpacingPolicy spacing = this.options.get(JTomlOption.SPACING);
        final LineSeparator newline = this.options.get(JTomlOption.LINE_SEPARATOR);
        if (writeComments && inline != null) {
            this.out.put(" # ");
            this.out.put(inline);
        }
        this.out.put(newline);
        if (writeComments) {
            for (Comment c : comments.get(CommentPosition.POST)) {
                this.out.put("# ");
                this.out.put(c.content());
                this.out.put(newline);
            }
        }
        for (int i=0; i < spacing.postStatement(); i++)
            this.out.put(newline);
    }

    private void writePrimitive(@NotNull TomlKey key, @NotNull TomlPrimitive value) throws TomlException {
        final Comments comments = value.comments();
        this.openStatement(key, comments);
        this.writePrimitiveValue(value);
        this.closeStatement(comments);
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
                this.out.put(Character.forDigit((c >> 12) & 0xF, 16));
                this.out.put(Character.forDigit((c >> 8) & 0xF, 16));
                this.out.put(Character.forDigit((c >> 4) & 0xF, 16));
                this.out.put(Character.forDigit(c & 0xF, 16));
            }
        }

        this.out.put('"');
    }

    private void writeArray(@NotNull TomlKey key, @NotNull TomlArray value) throws TomlException {
        final Comments comments = value.comments();
        this.openStatement(key, comments);
        this.writeArrayValue(value);
        this.closeStatement(comments);
    }

    private void writeArrayValue(@NotNull TomlArray value) throws TomlException {
        final ArrayStrategy strategy = this.options.get(JTomlOption.ARRAY_STRATEGY);
        boolean allowComments, doNewlines;
        switch (strategy) {
            case SHORT:
                allowComments = false;
                doNewlines = false;
                break;
            case TALL:
                allowComments = this.options.get(JTomlOption.WRITE_COMMENTS);
                doNewlines = true;
                break;
            case DYNAMIC:
                boolean anyCommented = false;
                boolean anyNonPrimitive = false;
                for (TomlValue child : value) {
                    anyCommented |= (child.comments().count() != 0);
                    anyNonPrimitive |= (!child.isPrimitive());
                }
                allowComments = this.options.get(JTomlOption.WRITE_COMMENTS) && anyCommented;
                doNewlines = (anyCommented || anyNonPrimitive);
                break;
            default:
                throw new AssertionError("Unreachable code");
        }

        final LineSeparator newline = this.options.get(JTomlOption.LINE_SEPARATOR);
        final PaddingPolicy padding = this.options.get(JTomlOption.PADDING);
        this.out.put('[');

        if (value.size() == 0) {
            this.out.put(']');
            return;
        }

        if (doNewlines) {
            this.out.put(newline);
            this.indentLevel++;
        } else {
            for (int i=0; i < padding.arrayPadding(); i++)
                this.out.put(' ');
        }

        final int al = value.size();
        TomlValue next;
        Comments nextComments;
        for (int i=0; i < al; i++) {
            next = value.get(i);
            nextComments = next.comments();

            if (doNewlines) this.writeIndent();
            if (allowComments) {
                for (Comment c : nextComments.get(CommentPosition.PRE)) {
                    this.out.put("# ");
                    this.out.put(c.content());
                    this.out.put(newline);
                    this.writeIndent();
                }
            }

            this.writeAnyValue(next);

            String inline;
            if (i != (al - 1)) {
                this.out.put(',');
                if (allowComments && (inline = nextComments.getInline()) != null) {
                    this.out.put(" # ");
                    this.out.put(inline);
                    this.out.put(newline);
                } else if (doNewlines) {
                    this.out.put(newline);
                } else {
                    for (int z=0; z < padding.elementPadding(); z++)
                        this.out.put(' ');
                }
            } else if (allowComments && (inline = nextComments.getInline()) != null) {
                this.out.put(" # ");
                this.out.put(inline);
                this.out.put(newline);
            } else if (doNewlines) {
                this.out.put(newline);
            }

            if (allowComments) {
                for (Comment c : nextComments.get(CommentPosition.POST)) {
                    this.writeIndent();
                    this.out.put("# ");
                    this.out.put(c.content());
                    this.out.put(newline);
                }
            }
        }

        if (doNewlines) {
            this.indentLevel--;
            this.writeIndent();
        } else {
            for (int i=0; i < padding.arrayPadding(); i++)
                this.out.put(' ');
        }

        this.out.put(']');
    }

    private void writeInlineTable(@NotNull TomlKey key, @NotNull TomlTable value) throws TomlException {
        final Comments comments = value.comments();
        this.openStatement(key, comments);
        this.writeInlineTableValue(value);
        this.closeStatement(comments);
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

    private @NotNull List<TypedKey> deconstruct(@NotNull TomlTable table) {
        SortMethod sort = this.options.get(JTomlOption.SORTING);
        switch (sort) {
            case STRATIFIED:
                return this.deconstructStratified(table);
            case LEXICOGRAPHICAL:
                return this.deconstructLexOrTime(table, false);
            case TIME:
                return this.deconstructLexOrTime(table, true);
            default:
                throw new AssertionError("Unreachable code");
        }
    }

    private @NotNull List<TypedKey> deconstructStratified(@NotNull TomlTable table) {
        final Set<TomlKey> all = table.keys(false);
        final int count = all.size();

        Map<ValueType, List<TomlKey>> map = new EnumMap<>(ValueType.class);
        for (TomlKey k : all) {
            TomlValue tv = table.get(k);
            assert tv != null;
            List<TomlKey> list = map.computeIfAbsent(
                    this.valueTypeOf(tv),
                    (ValueType ignored) -> new LinkedList<>()
            );
            list.add(k);
        }

        List<TypedKey> ret = new ArrayList<>(count);
        for (ValueType vt : ValueType.STRATA) {
            List<TomlKey> l = map.get(vt);
            if (l == null) continue;
            for (TomlKey k : l) {
                ret.add(new TypedKey(vt, k));
            }
        }

        return ret;
    }

    private @NotNull List<TypedKey> deconstructLexOrTime(@NotNull TomlTable table, boolean time) {
        final Set<TomlKey> all = table.keys(false);
        final int count = all.size();

        TypedKey[] buf = new TypedKey[count];
        int head = 0;
        for (TomlKey k : all) {
            TomlValue tv = table.get(k);
            assert tv != null;
            buf[head++] = new TypedKey(this.valueTypeOf(tv), k);
        }

        if (time) {
            Arrays.sort(
                    buf,
                    0, head,
                    (TypedKey k0, TypedKey k1) -> {
                        TomlValue v0 = table.get(k0.key);
                        TomlValue v1 = table.get(k1.key);
                        assert v0 != null && v1 != null;
                        return Long.compare(v0.creationTime(), v1.creationTime());
                    }
            );
        }

        rectifyKeyTypes(buf, head);
        return Arrays.asList(buf).subList(0, head);
    }

    @Contract(mutates = "param1")
    private void rectifyKeyTypes(@NotNull TypedKey @NotNull [] keys, int end) {
        boolean allowRich = true;
        for (int i = (end - 1); i >= 0; i--) {
            TypedKey tk = keys[i];
            if (allowRich) {
                if (tk.type == ValueType.PRIMITIVE || tk.type == ValueType.ARRAY)
                    allowRich = false;
            } else if (tk.type == ValueType.ARRAY_OF_TABLES) {
                keys[i] = new TypedKey(ValueType.ARRAY, tk.key);
            } else if (tk.type == ValueType.TABLE) {
                keys[i] = new TypedKey(ValueType.INLINE_TABLE, tk.key);
            }
        }
    }

    private @NotNull ValueType valueTypeOf(@NotNull TomlValue value) {
        if (value.isTable()) {
            return ValueType.TABLE;
        } else if (value.isArray()) {
            TomlArray a = value.asArray();
            int n = a.size();
            if (n == 0) return ValueType.ARRAY;
            for (int i=0; i < n; i++) {
                if (!a.get(i).isTable())
                    return ValueType.ARRAY;
            }
            return ValueType.ARRAY_OF_TABLES;
        } else {
            return ValueType.PRIMITIVE;
        }
    }

    @Override
    public void close() throws TomlException {
        this.out.close();
    }

    //

    private enum ValueType {
        PRIMITIVE,
        ARRAY,
        ARRAY_OF_TABLES,
        TABLE,
        INLINE_TABLE;

        static final ValueType[] STRATA = new ValueType[] {
                PRIMITIVE,
                ARRAY,
                ARRAY_OF_TABLES,
                TABLE
        };
    }

    private static final class TypedKey {

        private final ValueType type;
        private final TomlKey key;

        TypedKey(
                @NotNull ValueType type,
                @NotNull TomlKey key
        ) {
            this.type = type;
            this.key = key;
        }

    }

}
