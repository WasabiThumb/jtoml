/*
 * Copyright 2026 Xavier Pedraza
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

package io.github.wasabithumb.jtoml.util;

import org.jetbrains.annotations.Contract;

/**
 * Abstract supertype for all
 * classes which expose a {@link Builder Builder}
 * and can also be converted back to a
 * {@link Builder Builder} via {@link #toBuilder()}.
 * Concrete implementations should have a static
 * {@code builder()} method.
 */
public interface Buildable<T extends Buildable<T>> {

    /**
     * Applies the internal state of this
     * object to a newly created {@link Builder Builder}
     * which can be used to create a copy
     * or modified version of this object.
     * @return A new {@link Builder Builder}.
     */
    @Contract("-> new")
    Builder<T> toBuilder();

    //

    /**
     * A mutable object with an internal
     * state that can be used to create a
     * new typically immutable object
     * via {@link #build()}.
     */
    interface Builder<T extends Buildable<T>> {

        /**
         * Uses the internal state of this builder
         * to create a new object instance.
         * @return The new object.
         */
        @Contract("-> new")
        T build();

    }

}
