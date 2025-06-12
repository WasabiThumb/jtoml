package io.github.wasabithumb.jtoml.expression;

public final class EmptyExpression extends AbstractExpression {

    public static final EmptyExpression INSTANCE = new EmptyExpression();

    //

    @Override
    public boolean isEmpty() {
        return true;
    }

}
