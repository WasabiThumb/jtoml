package io.github.wasabithumb.jtoml.io;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.parse.TomlClobberException;
import io.github.wasabithumb.jtoml.except.parse.TomlExtensionException;
import io.github.wasabithumb.jtoml.expression.Expression;
import io.github.wasabithumb.jtoml.expression.KeyValueExpression;
import io.github.wasabithumb.jtoml.expression.TableExpression;
import io.github.wasabithumb.jtoml.io.source.BufferedCharSource;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.TomlValueFlags;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public final class TableReader extends ExpressionReader {

    public TableReader(@NotNull BufferedCharSource in, @NotNull JTomlOptions options) {
        super(in, options);
    }

    //

    public @NotNull TomlTable readTable() {
        TomlTable ret = TomlTable.create();
        Context ctx = new Context(ret, this.options.get(JTomlOption.EXTENSION_GUARD));
        Expression next;

        final boolean readComments = this.options.get(JTomlOption.READ_COMMENTS);
        List<String> comments = readComments ? new LinkedList<>() : null;
        TomlValue commentAttr = ret;

        while ((next = this.readExpression()) != null) {
            TomlValue defined;
            String comment;
            if (next.isKeyValue()) {
                defined = ctx.applyKeyValue(next.asKeyValue());
            } else if (next.isTable()) {
                defined = ctx.applyTable(next.asTable());
            } else {
                if (readComments && (comment = next.getComment()) != null)
                    comments.add(comment);
                continue;
            }
            if (readComments) {
                commentAttr = defined;
                Comments definedComments = defined.comments();
                if ((comment = next.getComment()) != null) definedComments.addInline(comment);
                for (String pre : comments) definedComments.addPre(pre);
                comments.clear();
            }
        }

        if (readComments && !comments.isEmpty()) {
            Comments attrComments = commentAttr.comments();
            for (String post : comments) attrComments.addPost(post);
        }

        return ret;
    }

    //

    private static final class Context {

        private final TomlTable global;
        private final boolean extGuard;
        private boolean useSub;
        private TomlKey subKey;
        private TomlTable subTable;

        Context(
                @NotNull TomlTable global,
                boolean extGuard
        ) {
            this.global = global;
            this.extGuard = extGuard;
            this.useSub = false;
            this.subKey = null;
            this.subTable = null;
        }

        //

        @NotNull TomlTable applyTable(@NotNull TableExpression e) throws TomlException {
            TomlValue head = this.global;
            TomlKey key = e.key();
            int ks = key.size();
            assert ks != 0;

            for (int i=0; i < (ks - 1); i++) {
                TomlKey last = TomlKey.literal(key.get(i));
                TomlValue next;
                if (head.isTable()) {
                    next = head.asTable().get(last);
                    if (next == null) {
                        next = TomlTable.create();
                        head.asTable().put(last, next);
                    }
                } else if (head.isArray()) {
                    TomlArray arr = head.asArray();
                    int len = arr.size();
                    if (len == 0 || !(next = arr.get(len - 1)).isTable() || TomlValueFlags.isConstant(next)) {
                        next = TomlTable.create();
                        arr.add(next);
                    }
                    TomlTable tbl = next.asTable();
                    next = tbl.get(last);
                    if (next == null) {
                        next = TomlTable.create();
                        tbl.put(last, next);
                    }
                } else {
                    throw new TomlClobberException("Defining table \"" + key + "\" would override primitive \"" +
                            key.slice(0, i + 1) + "\"");
                }

                if (this.extGuard && TomlValueFlags.isConstant(next)) {
                    throw new TomlExtensionException("Defining table \"" + key + "\" would extend constant value \"" +
                            key.slice(0, i + 1) + "\"");
                }
                head = next;
            }

            TomlTable table;
            if (head.isTable()) {
                table = head.asTable();
            } else if (head.isArray()) {
                TomlArray arr = head.asArray();
                int len = arr.size();
                TomlValue last;
                if (len == 0 || !(last = arr.get(len - 1)).isTable() || TomlValueFlags.isConstant(last)) {
                    table = TomlTable.create();
                    arr.add(table);
                } else {
                    table = last.asTable();
                }
            } else {
                throw new TomlClobberException("Defining table at \"" + key + "\" would override primitive at \"" +
                        key.slice(0, ks - 1) + "\"");
            }

            TomlKey name = TomlKey.literal(key.get(ks - 1));
            TomlTable newTable = TomlTable.create();
            TomlValue existing = table.get(name);

            if (e.isArray()) {
                TomlArray array;
                if (existing != null) {
                    if (!existing.isArray()) {
                        throw new TomlClobberException("Defining table array \"" + key +
                                "\" would override existing non-array");
                    }
                    if (this.extGuard && TomlValueFlags.isConstant(existing)) {
                        throw new TomlExtensionException("Defining table array \"" + key +
                                "\" extends existing constant array");
                    }
                    array = existing.asArray();
                } else {
                    array = TomlArray.create();
                    table.put(name, array);
                }
                array.add(newTable);
            } else {
                if (existing != null) {
                    if (!existing.isTable()) {
                        throw new TomlClobberException("Defining table \"" + key +
                                "\" would override existing non-table");
                    } else if (this.extGuard && TomlValueFlags.isConstant(existing)) {
                        throw new TomlExtensionException("Defining table \"" + key +
                                "\" extends existing constant table");
                    } else if (TomlValueFlags.isNonReusable(existing)) {
                        throw new TomlExtensionException("Reuse of explicitly defined table \"" + key + "\"");
                    }
                    newTable = existing.asTable();
                    TomlValueFlags.setNonReusable(newTable, true);
                } else {
                    TomlValueFlags.setNonReusable(newTable, true);
                    TomlValueFlags.setNonKeyExtendable(newTable, true);
                    table.put(name, newTable);
                }
            }

            this.useSub = true;
            this.subTable = newTable;
            this.subKey = key;
            return newTable;
        }

        @NotNull TomlValue applyKeyValue(@NotNull KeyValueExpression e) throws TomlException {
            TomlTable target = this.useSub ? this.subTable : this.global;
            TomlKey key = e.key();
            TomlValue value = e.value();

            int kl = key.size();
            for (int i=0; i < (kl - 1); i++) {
                TomlKey part = TomlKey.literal(key.get(i));
                TomlValue next = target.get(part);
                if (next == null) {
                    TomlTable sub = TomlTable.create();
                    target.put(part, TomlValueFlags.setNonReusable(sub, true));
                    target = sub;
                    continue;
                }
                if (next.isTable()) {
                    if (this.extGuard && TomlValueFlags.isConstant(next)) {
                        throw new TomlExtensionException("Defining value \"" + this.fullKey(key) +
                                "\" would extend constant table \"" + this.fullKey(key.slice(0, i + 1)) + "\"");
                    }
                    if (this.extGuard && TomlValueFlags.isNonKeyExtendable(next)) {
                        throw new TomlExtensionException("Cannot extend table \"" + this.fullKey(key.slice(0, i + 1)) +
                                "\" (defining key " + this.fullKey(key) + ")");
                    }
                    target = next.asTable();
                    continue;
                }
                throw new TomlClobberException("Defining value \"" + this.fullKey(key) +
                        "\" would override non-table \"" + this.fullKey(key.slice(0, i + 1)) + "\"");
            }

            TomlKey name = TomlKey.literal(key.get(kl - 1));
            if (target.contains(name))
                throw new TomlClobberException("Attempt to re-define \"" + this.fullKey(key) + "\"");

            // Mark arrays & inline tables defined this way as constant
            if (!value.isPrimitive()) TomlValueFlags.setConstant(value, true);
            target.put(name, value);
            return value;
        }

        private @NotNull TomlKey fullKey(@NotNull TomlKey key) {
            return this.useSub ?
                    TomlKey.join(this.subKey, key) :
                    key;
        }

    }

}
