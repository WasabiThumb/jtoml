package io.github.wasabithumb.jtoml.expression;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractExpression implements Expression {

    protected String comment = null;

    //

    @Override
    public @Nullable String getComment() {
        return this.comment;
    }

    @Override
    public void setComment(@Nullable String comment) {
        this.comment = comment;
    }

}
