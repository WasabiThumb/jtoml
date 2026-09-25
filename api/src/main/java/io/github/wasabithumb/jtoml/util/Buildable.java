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
