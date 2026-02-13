package io.github.wasabithumb.jtoml.route;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.option.JTomlOptions;

public interface TestRoute {

    String displayName();

    void execute(JToml instance);

    //

    interface Failing<X extends Throwable> extends TestRoute {

        Class<? extends X> errorType();

        void executeFailing(JToml instance) throws X;

        @Override
        default void execute(JToml instance) {
            Throwable t = null;
            try {
                this.executeFailing(instance);
            } catch (Throwable error) {
                t = error;
            }

            final Class<? extends X> errorType = this.errorType();
            if (t == null) {
                throw new AssertionError(
                        "Route " + this.displayName() +
                                " did not raise " + errorType.getName()
                );
            } else if (!errorType.isInstance(t)) {
                throw new AssertionError(
                        "Route" + this.displayName() +
                                " raised " + t.getClass().getName() +
                                ", which is not an instance of " + errorType.getName(),
                        t
                );
            }
        }

    }

    interface Configuring extends TestRoute {

        void configure(JTomlOptions.Builder options);

    }

}
