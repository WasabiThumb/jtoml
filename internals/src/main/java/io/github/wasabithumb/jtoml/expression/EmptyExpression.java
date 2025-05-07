package io.github.wasabithumb.jtoml.expression;

public final class EmptyExpression implements Expression {

    public static final EmptyExpression INSTANCE = new EmptyExpression();

    //

    @Override
    public boolean isEmpty() {
        return true;
    }

}
