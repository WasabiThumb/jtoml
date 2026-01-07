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

package io.github.wasabithumb.jtoml.option.prop;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Determines the amount of space characters to insert
 * within TOML elements
 */
public final class PaddingPolicy {

    /** No padding */
    public static final PaddingPolicy NONE     = new PaddingPolicy(0x00000000);

    /** Do not pad table headers; pad inline tables, arrays and their elements by 1 */
    public static final PaddingPolicy STANDARD = new PaddingPolicy(0x00010101);

    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    //

    private final int data;

    private PaddingPolicy(int data) {
        this.data = data;
    }

    //

    public @Range(from=0, to=255) int tablePadding() {
        return (this.data >> 24) & 0xFF;
    }

    public @Range(from=0, to=255) int inlineTablePadding() {
        return (this.data >> 16) & 0xFF;
    }

    public @Range(from=0, to=255) int arrayPadding() {
        return (this.data >> 8) & 0xFF;
    }

    public @Range(from=0, to=255) int elementPadding() {
        return this.data & 0xFF;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.data);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PaddingPolicy)) return false;
        return this.data == ((PaddingPolicy) obj).data;
    }

    @Override
    public @NotNull String toString() {
        return "PaddingPolicy[tablePadding=" + this.tablePadding() +
                ", inlineTablePadding=" + this.inlineTablePadding() +
                ", arrayPadding=" + this.arrayPadding() +
                ", elementPadding=" + this.elementPadding() +
                "]";
    }

    //

    public static final class Builder {

        private final int[] values = new int[4];

        //

        @Contract("_, _ -> this")
        private @NotNull Builder set(int index, int value) {
            if (value < 0) throw new IllegalArgumentException("Padding may not be negative");
            if (value > 255) throw new IllegalArgumentException("Padding is too large (" + value + " > 255)");
            this.values[index] = value;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder tablePadding(@Range(from=0, to=255) int padding) {
            return this.set(0, padding);
        }

        @Contract("_ -> this")
        public @NotNull Builder inlineTablePadding(@Range(from=0, to=255) int padding) {
            return this.set(1, padding);
        }

        @Contract("_ -> this")
        public @NotNull Builder arrayPadding(@Range(from=0, to=255) int padding) {
            return this.set(2, padding);
        }

        @Contract("_ -> this")
        public @NotNull Builder elementPadding(@Range(from=0, to=255) int padding) {
            return this.set(3, padding);
        }

        @Contract("-> new")
        public @NotNull PaddingPolicy build() {
            return new PaddingPolicy(
                    (this.values[0] << 24) | (this.values[1] << 16) |
                            (this.values[2] << 8) | this.values[3]
            );
        }

    }

}
