package io.github.wasabithumb.jtoml.io;

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
import io.github.wasabithumb.jtoml.value.FlaggedTomlValue;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.NotNull;

public final class TableReader extends ExpressionReader {

    public TableReader(@NotNull BufferedCharSource in, @NotNull JTomlOptions options) {
        super(in, options);
    }

    //

    public @NotNull TomlTable readTable() {
        TomlTable ret = TomlTable.create();
        Context ctx = new Context(ret, this.options.get(JTomlOption.EXTENSION_GUARD));
        Expression next;

        while ((next = this.readExpression()) != null) {
            if (next.isKeyValue()) {
                ctx.applyKeyValue(next.asKeyValue());
            } else if (next.isTable()) {
                ctx.applyTable(next.asTable());
            }
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

        void applyTable(@NotNull TableExpression e) throws TomlException {
            TomlValue head = this.global;
            TomlKey key = e.key();
            int ks = key.size();
            if (ks == 0) return;

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
                    if (len == 0 || !(next = arr.get(len - 1)).isTable() || FlaggedTomlValue.isConstant(next)) {
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

                if (this.extGuard && FlaggedTomlValue.isConstant(next)) {
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
                if (len == 0 || !(last = arr.get(len - 1)).isTable() || FlaggedTomlValue.isConstant(last)) {
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

            if (e.isArray()) {
                TomlValue existing = table.get(name);
                TomlArray array;
                if (existing != null) {
                    if (!existing.isArray()) {
                        throw new TomlClobberException("Defining table array \"" + key +
                                "\" would override existing non-array");
                    }
                    if (this.extGuard && FlaggedTomlValue.isConstant(existing)) {
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
                TomlValue existing = table.get(name);
                if (existing != null) {
                    if (!existing.isTable()) {
                        throw new TomlClobberException("Defining table \"" + key +
                                "\" would override existing non-table");
                    } else if (this.extGuard && FlaggedTomlValue.isConstant(existing)) {
                        throw new TomlExtensionException("Defining table \"" + key +
                                "\" extends existing constant table");
                    } else if (FlaggedTomlValue.isNonReusable(existing)) {
                        throw new TomlExtensionException("Reuse of explicitly defined table \"" + key + "\"");
                    }
                    newTable = existing.asTable();
                    table.remove(name);

                    FlaggedTomlValue flagged = FlaggedTomlValue.wrap(existing);
                    flagged.setNonReusable(true);
                    table.put(name, flagged);
                } else {
                    FlaggedTomlValue flagged = FlaggedTomlValue.wrap(newTable);
                    flagged.setNonReusable(true);
                    flagged.setNonKeyExtendable(true);
                    table.put(name, flagged);
                }
            }

            this.useSub = true;
            this.subTable = newTable;
            this.subKey = key;
        }

        void applyKeyValue(@NotNull KeyValueExpression e) throws TomlException {
            TomlTable target = this.useSub ? this.subTable : this.global;
            TomlKey key = e.key();
            TomlValue value = e.value();

            int kl = key.size();
            for (int i=0; i < (kl - 1); i++) {
                TomlKey part = TomlKey.literal(key.get(i));
                TomlValue next = target.get(part);
                if (next == null) {
                    TomlTable sub = TomlTable.create();
                    FlaggedTomlValue flagged = FlaggedTomlValue.wrap(sub);
                    flagged.setNonReusable(true);
                    target.put(part, flagged);
                    target = sub;
                    continue;
                }
                if (next.isTable()) {
                    if (this.extGuard && FlaggedTomlValue.isConstant(next)) {
                        throw new TomlExtensionException("Defining value \"" + this.fullKey(key) +
                                "\" would extend constant table \"" + this.fullKey(key.slice(0, i + 1)) + "\"");
                    }
                    if (this.extGuard && FlaggedTomlValue.isNonKeyExtendable(next)) {
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
            if (!value.isPrimitive()) {
                FlaggedTomlValue flaggedValue = FlaggedTomlValue.wrap(value);
                flaggedValue.setConstant(true);
                value = flaggedValue;
            }
            target.put(name, value);
        }

        private @NotNull TomlKey fullKey(@NotNull TomlKey key) {
            return this.useSub ?
                    TomlKey.join(this.subKey, key) :
                    key;
        }

    }

}
