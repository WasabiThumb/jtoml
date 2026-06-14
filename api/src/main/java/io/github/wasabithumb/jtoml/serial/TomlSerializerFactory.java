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

package io.github.wasabithumb.jtoml.serial;

import io.github.wasabithumb.jtoml.JToml;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Creates {@link TomlSerializer} instances
 * for some set of Java types. Intended
 * to be instantiated through the service
 * loading mechanism. Replaces the deprecated
 * {@link TomlSerializerService}.
 */
public abstract class TomlSerializerFactory {

    /**
     * Compatibility bridge for the deprecated
     * {@link TomlSerializerService} class. This method will be
     * removed when the class is removed.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @Contract("_ -> new")
    public static @NotNull TomlSerializerFactory of(@NotNull TomlSerializerService legacy) {
        return new LegacyAdapter(legacy);
    }

    //

    /**
     * Attempts to facilitate the creation of a serializer which
     * respects the configuration of the provided {@code instance}
     * and converts {@link io.github.wasabithumb.jtoml.value.table.TomlTable TomlTable}s to
     * objects of type {@code outType}.
     * If this factory does not support the given type,
     * returns an {@link Result#valid() invalid} result.
     */
    public abstract <T> @NotNull Result<?, T> fromToml(@NotNull JToml instance, @NotNull Class<T> outType);

    /**
     * Attempts to facilitate the creation of a serializer which
     * respects the configuration of the provided {@code instance}
     * and converts objects of type {@code inType} to
     * {@link io.github.wasabithumb.jtoml.value.table.TomlTable TomlTable}s.
     * If this factory does not support the given type,
     * returns an {@link Result#valid() invalid} result.
     */
    public abstract <T> @NotNull Result<T, ?> toToml(@NotNull JToml instance, @NotNull Class<T> inType);

    //

    @ApiStatus.NonExtendable
    public static abstract class Result<I, O> {

        @Contract("_ -> new")
        public static <II, OO> Result<II, OO> valid(@NotNull Supplier<TomlSerializer<II, OO>> supplier) {
            Objects.requireNonNull(supplier, "supplier");
            return new Valid<>(supplier);
        }

        @Contract("_ -> new")
        public static <II, OO> Result<II, OO> valid(@NotNull TomlSerializer<II, OO> serializer) {
            Objects.requireNonNull(serializer, "serializer");
            return new Valid<>(() -> serializer);
        }

        @SuppressWarnings("unchecked")
        public static <II, OO> Result<II, OO> invalid() {
            return (Result<II, OO>) Invalid.INSTANCE;
        }

        //

        public abstract boolean valid();

        public abstract @NotNull TomlSerializer<I, O> serializer();

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
            public TomlSerializer<I, O> serializer() {
                return this.supplier.get();
            }

        }

        private static final class Invalid<I, O> extends Result<I, O> {

            private static final Invalid<?, ?> INSTANCE = new Invalid<>();

            private Invalid() { }

            //

            @Override
            public boolean valid() {
                return false;
            }

            @Override
            public TomlSerializer<I, O> serializer() {
                throw new UnsupportedOperationException("cannot get serializer from invalid result");
            }

        }

    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    private static final class LegacyAdapter extends TomlSerializerFactory {

        private final TomlSerializerService handle;

        private LegacyAdapter(TomlSerializerService handle) {
            this.handle = handle;
        }

        //

        @Override
        public <T> Result<?, T> fromToml(JToml instance, Class<T> outType) {
            if (!this.handle.canSerializeTo(outType)) return Result.invalid();
            return Result.valid(this.handle.getSerializer(instance, outType));
        }

        @Override
        public <T> Result<T, ?> toToml(JToml instance, Class<T> inType) {
            if (!this.handle.canDeserializeFrom(inType)) return Result.invalid();
            return Result.valid(this.handle.getDeserializer(instance, inType));
        }

    }

}
