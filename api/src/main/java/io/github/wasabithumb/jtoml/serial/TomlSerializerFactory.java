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

package io.github.wasabithumb.jtoml.serial;

import io.github.wasabithumb.jtoml.JToml;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Creates {@link TomlSerializer} instances
 * for some set of Java types. Intended
 * to be instantiated through the service
 * loading mechanism.
 */
@ApiStatus.AvailableSince("1.6.0")
public abstract class TomlSerializerFactory {

    /**
     * Attempts to facilitate the creation of a serializer which
     * respects the configuration of the provided {@code instance}
     * and converts {@link io.github.wasabithumb.jtoml.value.table.TomlTable TomlTable}s to
     * objects of type {@code outType}.
     * If this factory does not support the given type,
     * returns an {@link Result#valid() invalid} result.
     */
    public abstract <T> Result<?, T> fromToml(JToml instance, Class<T> outType);

    /**
     * Attempts to facilitate the creation of a serializer which
     * respects the configuration of the provided {@code instance}
     * and converts objects of type {@code inType} to
     * {@link io.github.wasabithumb.jtoml.value.table.TomlTable TomlTable}s.
     * If this factory does not support the given type,
     * returns an {@link Result#valid() invalid} result.
     */
    public abstract <T> Result<T, ?> toToml(JToml instance, Class<T> inType);

    //

    /**
     * Result of a call to {@link #fromToml(JToml, Class) fromToml}
     * or {@link #toToml(JToml, Class) toToml}. If the result
     * is {@link #valid() valid}, a serializer can be obtained
     * through the {@link #serializer()} method. Otherwise,
     * an issue described by the {@link #issue()} method may
     * be read.
     */
    @ApiStatus.NonExtendable
    public static abstract class Result<I, O> {

        @Contract("_ -> new")
        public static <II, OO> Result<II, OO> valid(Supplier<TomlSerializer<II, OO>> supplier) {
            Objects.requireNonNull(supplier, "supplier");
            return new Valid<>(supplier);
        }

        @Contract("_ -> new")
        public static <II, OO> Result<II, OO> valid(TomlSerializer<II, OO> serializer) {
            Objects.requireNonNull(serializer, "serializer");
            return new Valid<>(() -> serializer);
        }

        @Contract("_ -> new")
        public static <II, OO> Result<II, OO> invalid(String issue) {
            return new Invalid<>(issue);
        }

        //

        /**
         * Reports the validity of this result.
         * @return True if valid.
         * @see #serializer()
         * @see #issue()
         */
        public abstract boolean valid();

        /**
         * Reports the issue message stored
         * in this result if it is not valid.
         * @return The stored issue message.
         * @throws UnsupportedOperationException This result is {@link #valid() valid}.
         */
        public abstract String issue() throws UnsupportedOperationException;

        /**
         * Reports the {@link TomlSerializer serializer} stored
         * in this result if it is valid.
         * @return The stored {@link TomlSerializer serializer}.
         * @throws UnsupportedOperationException This result is not {@link #valid() valid}.
         */
        public abstract TomlSerializer<I, O> serializer() throws UnsupportedOperationException;

        //

        private static final class Valid<I, O> extends Result<I, O> {

            private final Supplier<TomlSerializer<I, O>> supplier;

            private Valid(Supplier<TomlSerializer<I, O>> supplier) {
                this.supplier = supplier;
            }

            //

            @Override
            public boolean valid() {
                return true;
            }

            @Override
            public String issue() {
                throw new UnsupportedOperationException("valid result has no issue");
            }

            @Override
            public TomlSerializer<I, O> serializer() {
                return this.supplier.get();
            }

        }

        private static final class Invalid<I, O> extends Result<I, O> {

            private final String issue;

            private Invalid(String issue) {
                this.issue = issue;
            }

            //

            @Override
            public boolean valid() {
                return false;
            }

            @Override
            public String issue() {
                return this.issue;
            }

            @Override
            public TomlSerializer<I, O> serializer() {
                throw new UnsupportedOperationException("cannot get serializer from invalid result");
            }

        }

    }

}
