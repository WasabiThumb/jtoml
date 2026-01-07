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

import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An abstraction for facets which can transform
 * TOML tables to/from some other type in-memory.
 *
 * <table border="1">
 *     <caption>Available Serializers</caption>
 *     <tr>
 *         <th>Name</th>
 *         <th>Artifact</th>
 *         <th>Supported Type(s)</th>
 *     </tr>
 *     <tr>
 *         <td>{@code PlainTextTomlSerializer}</td>
 *         <td>N/A</td>
 *         <td>{@link String}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code GsonTomlSerializer}</td>
 *         <td>{@code jtoml-serializer-gson}</td>
 *         <td><a href="https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/JsonObject.html">{@code JsonObject}</a></td>
 *     </tr>
 *     <tr>
 *         <td>{@code ReflectTomlSerializer}</td>
 *         <td>{@code jtoml-serializer-reflect}</td>
 *         <td>
 *             {@link TomlSerializable},
 *             {@link java.util.Map Map&lt;String, ?&gt;},
 *             {@link java.util.List List&lt;?&gt;},
 *             <a href="https://openjdk.org/jeps/395">records</a>,
 *             primitives (boxed and unboxed) and
 *             arrays
 *         </td>
 *     </tr>
 * </table>
 */
public interface TomlSerializer<I, O> {

    @ApiStatus.OverrideOnly
    @NotNull Class<I> inType();

    @ApiStatus.OverrideOnly
    @NotNull Class<O> outType();

    /**
     * @deprecated Use {@link #fromToml(TomlTable)}
     */
    @Deprecated
    @NotNull O serialize(@NotNull TomlTable table);

    @ApiStatus.AvailableSince("1.2.1")
    default @NotNull O fromToml(@NotNull TomlTable table) {
        return this.serialize(table);
    }

    /**
     * @deprecated Use {@link #toToml(Object)}
     */
    @Deprecated
    @NotNull TomlTable deserialize(@NotNull I data);

    @ApiStatus.AvailableSince("1.2.1")
    default @NotNull TomlTable toToml(@NotNull I data) {
        return this.deserialize(data);
    }

    //

    /**
     * A {@link TomlSerializer} which serializes and deserializes
     * the same type
     */
    interface Symmetric<T> extends TomlSerializer<T, T> {

        @ApiStatus.OverrideOnly
        @NotNull Class<T> serialType();

        @Override
        default @NotNull Class<T> inType() {
            return this.serialType();
        }

        @Override
        default @NotNull Class<T> outType() {
            return this.serialType();
        }

    }

}
