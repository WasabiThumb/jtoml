/*
 * Copyright 2025 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                error.printStackTrace(System.err);
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
