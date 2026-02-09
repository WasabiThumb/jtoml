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

import io.github.wasabithumb.jtoml.key.convention.StandardKeyConvention;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Annotation for use in {@link io.github.wasabithumb.jtoml.serial.TomlSerializable reflect serialization}.
 * Overrides the {@link io.github.wasabithumb.jtoml.key.convention.KeyConvention key convention} in use
 * at the class/record or member level. Due to JVM restrictions, only the
 * {@link StandardKeyConvention standard conventions} may be used with this annotation.
 *
 * @see Literal
 * @see Lower
 * @see Kebab
 * @see Snake
 * @see Split
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Convention {
    @NotNull StandardKeyConvention value();

    //

    /**
     * Identical to {@code @Convention(KeyConvention.LITERAL)}
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
    @interface Literal {
        @SuppressWarnings("unused")
        StandardKeyConvention VALUE = StandardKeyConvention.LITERAL;
    }

    /**
     * Identical to {@code @Convention(KeyConvention.LOWER)}
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
    @interface Lower {
        @SuppressWarnings("unused")
        StandardKeyConvention VALUE = StandardKeyConvention.LOWER;
    }

    /**
     * Identical to {@code @Convention(KeyConvention.KEBAB)}
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
    @interface Kebab {
        @SuppressWarnings("unused")
        StandardKeyConvention VALUE = StandardKeyConvention.KEBAB;
    }

    /**
     * Identical to {@code @Convention(KeyConvention.SNAKE)}
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
    @interface Snake {
        @SuppressWarnings("unused")
        StandardKeyConvention VALUE = StandardKeyConvention.SNAKE;
    }

    /**
     * Identical to {@code @Convention(KeyConvention.SPLIT)}
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
    @ApiStatus.Experimental
    @interface Split {
        @SuppressWarnings("unused")
        StandardKeyConvention VALUE = StandardKeyConvention.SPLIT;
    }

}
