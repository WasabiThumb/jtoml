package io.github.wasabithumb.jtoml.expression;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.NotNull;

public final class KeyValueExpression extends AbstractExpression {

    private final TomlKey key;
    private final TomlValue value;

    public KeyValueExpression(@NotNull TomlKey key, @NotNull TomlValue value) {
        this.key = key;
        this.value = value;
    }

    //

    public @NotNull TomlKey key() {
        return this.key;
    }

    public @NotNull TomlValue value() {
        return this.value;
    }

    @Override
    public boolean isKeyValue() {
        return true;
    }

}
