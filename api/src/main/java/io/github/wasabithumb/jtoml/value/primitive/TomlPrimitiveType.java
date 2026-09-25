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

package io.github.wasabithumb.jtoml.value.primitive;

/**
 * Represents the type of a {@link TomlPrimitive TOML primitive};
 * values that are not a table or array.
 */
public enum TomlPrimitiveType {
    /**
     * Primitives with this {@link TomlPrimitive#type() type}
     * have a {@link TomlPrimitive#value() value} of Java type {@link String}.
     */
    STRING,

    /**
     * Primitives with this {@link TomlPrimitive#type() type}
     * have a {@link TomlPrimitive#value() value} of Java type {@link Boolean}.
     */
    BOOLEAN,

    /**
     * Primitives with this {@link TomlPrimitive#type() type}
     * have a {@link TomlPrimitive#value() value} of Java type {@link Long}.
     */
    INTEGER,

    /**
     * Primitives with this {@link TomlPrimitive#type() type}
     * have a {@link TomlPrimitive#value() value} of Java type {@link Double}.
     */
    FLOAT,

    /**
     * Primitives with this {@link TomlPrimitive#type() type}
     * have a {@link TomlPrimitive#value() value} of Java type {@link java.time.OffsetDateTime}.
     */
    OFFSET_DATE_TIME,

    /**
     * Primitives with this {@link TomlPrimitive#type() type}
     * have a {@link TomlPrimitive#value() value} of Java type {@link java.time.LocalDateTime}.
     */
    LOCAL_DATE_TIME,

    /**
     * Primitives with this {@link TomlPrimitive#type() type}
     * have a {@link TomlPrimitive#value() value} of Java type {@link java.time.LocalDate}.
     */
    LOCAL_DATE,

    /**
     * Primitives with this {@link TomlPrimitive#type() type}
     * have a {@link TomlPrimitive#value() value} of Java type {@link java.time.LocalTime}.
     */
    LOCAL_TIME
}
