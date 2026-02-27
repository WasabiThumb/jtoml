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

package io.github.wasabithumb.jtoml.serial.reflect;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.*;

/**
 * <p>
 *     Annotation for use in {@link io.github.wasabithumb.jtoml.serial.TomlSerializable reflect serialization}.
 *     Permits partial resolution of a serializable type. When applied to a single field/component, the matching
 *     key does not have to exist in the source document. When applied to the type itself,
 *     none of its fields/components have to exist in the source document.
 * </p>
 * <p>
 *     When an optional specific subtype is used, a default value may be specified.
 *     This covers all types which may be used as an annotation parameter per
 *     JVM restrictions:
 * </p>
 * <table>
 *     <caption>Optional Specific Subtypes</caption>
 *     <tr>
 *         <th>Annotation</th>
 *         <th>Applicable Types</th>
 *     </tr>
 *     <tr>
 *         <td>{@link ToString @Defaulting.ToString}</td>
 *         <td>{@code String}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link ToInt @Defaulting.ToInt}</td>
 *         <td>{@code byte}, {@code short}, {@code int}, {@code long}, {@code char} and boxed variants</td>
 *     </tr>
 *     <tr>
 *         <td>{@link ToFloat @Defaulting.ToFloat}</td>
 *         <td>{@code float}, {@code double} and boxed variants</td>
 *     </tr>
 *     <tr>
 *         <td>{@link ToBool @Defaulting.ToBool}</td>
 *         <td>{@code boolean} and boxed variant</td>
 *     </tr>
 * </table>
 * <p>
 *     For <strong>record components</strong> with <strong>no mapping</strong>
 *     and <strong>in absence of an optional specific subtype</strong>,
 *     the "default value" for that type is injected. This coincides
 *     with how the compiler tends to resolve uninitialized fields,
 *     shown below:
 * </p>
 * <table>
 *     <caption>Default Values</caption>
 *     <tr>
 *         <th>Type</th>
 *         <th>Default Value</th>
 *     </tr>
 *     <tr>
 *         <td>{@code byte}</td>
 *         <td>{@code 0}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code short}</td>
 *         <td>{@code 0}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code int}</td>
 *         <td>{@code 0}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code long}</td>
 *         <td>{@code 0L}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code float}</td>
 *         <td>{@code 0f}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code double}</td>
 *         <td>{@code 0d}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code char}</td>
 *         <td>{@code (char) 0}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code boolean}</td>
 *         <td>{@code false}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code Object}</td>
 *         <td>{@code null}</td>
 *     </tr>
 * </table>
 * <p>
 *     For <strong>POJO fields</strong> with <strong>no mapping</strong>
 *     and <strong>in absence of an optional specific subtype</strong>,
 *     the field is left uninitialized. The field will retain the
 *     value it has at initialization time.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@ApiStatus.AvailableSince("1.5.1")
public @interface Defaulting {

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface ToString {
        String value();
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface ToInt {
        long value();
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface ToFloat {
        double value();
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface ToBool {
        boolean value();
    }

}
