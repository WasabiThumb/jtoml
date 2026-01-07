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
 * Determines how newlines should be inserted to separate
 * TOML elements
 */
public final class SpacingPolicy {

    /** No spacing */
    public static final SpacingPolicy NONE     = new SpacingPolicy(0x00000000);

    /** Put 1 newline before each table */
    public static final SpacingPolicy STANDARD = new SpacingPolicy(0x01000000);

    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    //

    private final int data;

    private SpacingPolicy(int data) {
        this.data = data;
    }

    //

    public @Range(from=0, to=255) int preTable() {
        return (this.data >> 24) & 0xFF;
    }

    public @Range(from=0, to=255) int postTable() {
        return (this.data >> 16) & 0xFF;
    }

    public @Range(from=0, to=255) int preStatement() {
        return (this.data >> 8) & 0xFF;
    }

    public @Range(from=0, to=255) int postStatement() {
        return this.data & 0xFF;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.data);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SpacingPolicy)) return false;
        return this.data == ((SpacingPolicy) obj).data;
    }

    @Override
    public @NotNull String toString() {
        return "SpacingPolicy[preTable=" + this.preTable() +
                ", postTable=" + this.postTable() +
                ", preStatement=" + this.preStatement() +
                ", postStatement=" + this.postStatement() +
                "]";
    }

    //

    public static final class Builder {

        private final int[] values = new int[4];

        //

        @Contract("_, _ -> this")
        private @NotNull Builder set(int index, int value) {
            if (value < 0) throw new IllegalArgumentException("Spacing may not be negative");
            if (value > 255) throw new IllegalArgumentException("Spacing is too large (" + value + " > 255)");
            this.values[index] = value;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder preTable(@Range(from=0, to=255) int spacing) {
            return this.set(0, spacing);
        }

        @Contract("_ -> this")
        public @NotNull Builder postTable(@Range(from=0, to=255) int spacing) {
            return this.set(1, spacing);
        }

        @Contract("_ -> this")
        public @NotNull Builder preStatement(@Range(from=0, to=255) int spacing) {
            return this.set(2, spacing);
        }

        @Contract("_ -> this")
        public @NotNull Builder postStatement(@Range(from=0, to=255) int spacing) {
            return this.set(3, spacing);
        }

        @Contract("-> new")
        public @NotNull SpacingPolicy build() {
            return new SpacingPolicy(
                    (this.values[0] << 24) | (this.values[1] << 16) |
                            (this.values[2] << 8) | this.values[3]
            );
        }

    }

}
