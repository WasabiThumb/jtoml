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

package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * An immutable collection of {@link TypeAdapter}s for use
 * in {@link io.github.wasabithumb.jtoml.serial.reflect.ReflectTomlSerializer reflect serialization}.
 * @see #builder()
 * @see #standard()
 */
@ApiStatus.AvailableSince("1.4.1")
@ApiStatus.NonExtendable
public interface TypeAdapters extends Collection<TypeAdapter<?>> {

    /**
     * Provides a new builder for creating
     * custom {@link TypeAdapter} collections.
     * This builder is initially empty and does not
     * contain any of the
     * {@link #standard() standard adapters}.
     */
    @Contract("-> new")
    static @NotNull Builder builder() {
        return new TypeAdaptersImpl.Builder();
    }

    /**
     * Provides a {@link TypeAdapters} instance
     * containing every constant defined
     * in {@link TypeAdapter}. This is the default
     * set of adapters used by the reflect serializer.
     */
    @Contract(pure = true)
    static @NotNull TypeAdapters standard() {
        return TypeAdaptersImpl.STANDARD;
    }

    //

    @ApiStatus.Internal
    <T> @Nullable TypeAdapter<T> get(@NotNull Class<T> type);

    //

    /**
     * Handles creation of custom {@link TypeAdapters}
     * collections.
     * @see #clear()
     * @see #add(TypeAdapter)
     * @see #add(Collection)
     * @see #build()
     */
    @ApiStatus.NonExtendable
    interface Builder {

        /**
         * Clears the builder,
         * removing all adapters previously
         * added to it.
         */
        @Contract("-> this")
        @NotNull Builder clear();

        /**
         * Adds a new type adapter.
         * If an adapter with the same {@link TypeAdapter#typeClass() type class}
         * was already added, that adapter is replaced.
         * If the adapter's type class is
         * primitive, it is additionally added as if it were
         * the boxed variant.
         */
        @Contract("_ -> this")
        @NotNull Builder add(@NotNull TypeAdapter<?> adapter);

        /**
         * Adds each type adapter contained in the
         * given collection to this builder.
         * Can be used to inherit the standard adapters
         * via {@code add(TypeAdapters.standard())}.
         * @see #add(TypeAdapter)
         */
        @Contract("_ -> this")
        default @NotNull Builder add(@NotNull Collection<TypeAdapter<?>> adapters) {
            for (TypeAdapter<?> adapter : adapters) {
                this.add(adapter);
            }
            return this;
        }

        /**
         * Provides a new {@link TypeAdapters} instance
         * containing all adapters added to this builder.
         * The builder may not be used after this point.
         */
        @Contract("-> new")
        @NotNull TypeAdapters build();

    }

}
