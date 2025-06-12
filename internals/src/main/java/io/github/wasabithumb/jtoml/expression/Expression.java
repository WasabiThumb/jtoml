package io.github.wasabithumb.jtoml.expression;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Expression {

    static @NotNull EmptyExpression empty() {
        return EmptyExpression.INSTANCE;
    }

    static @NotNull KeyValueExpression keyValue(@NotNull TomlKey key, @NotNull TomlValue value) {
        return new KeyValueExpression(key, value);
    }

    static @NotNull TableExpression table(@NotNull TomlKey key, boolean isArray) {
        return new TableExpression(key, isArray);
    }

    //

    @Nullable String getComment();

    void setComment(@Nullable String comment);

    default boolean isEmpty() {
        return false;
    }

    @Contract("-> this")
    default @NotNull EmptyExpression asEmpty() {
        return (EmptyExpression) this;
    }

    default boolean isKeyValue() {
        return false;
    }

    @Contract("-> this")
    default @NotNull KeyValueExpression asKeyValue() {
        return (KeyValueExpression) this;
    }

    default boolean isTable() {
        return false;
    }

    @Contract("-> this")
    default @NotNull TableExpression asTable() {
        return (TableExpression) this;
    }

}
