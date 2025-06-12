package io.github.wasabithumb.jtoml.expression;

import io.github.wasabithumb.jtoml.key.TomlKey;
import org.jetbrains.annotations.NotNull;

public final class TableExpression extends AbstractExpression {

    private final TomlKey key;
    private final boolean isArray;

    public TableExpression(@NotNull TomlKey key, boolean isArray) {
        this.key = key;
        this.isArray = isArray;
    }

    //

    public @NotNull TomlKey key() {
        return this.key;
    }

    public boolean isArray() {
        return this.isArray;
    }

    @Override
    public boolean isTable() {
        return true;
    }

}
