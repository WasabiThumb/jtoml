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

package io.github.wasabithumb.jtoml.comment;

import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Represents a TOML comment.
 * Also holds helper annotations for use in reflect serialization
 * ({@link Pre @Comment.Pre}, {@link Post @Comment.Post}, {@link Inline @Comment.Inline}),
 * not to be confused with the {@link CommentPosition} enum constants.
 * @see #position()
 * @see #content()
 */
@ApiStatus.NonExtendable
@ApiStatus.AvailableSince("0.6.0")
public interface Comment {

    @Contract("_, _ -> new")
    static @NotNull Comment of(@NotNull CommentPosition position, @NotNull String content) {
        return new CommentImpl(position, content);
    }

    //

    /**
     * Position of the comment
     */
    @NotNull CommentPosition position();

    /**
     * Raw text contained within the comment
     */
    @NotNull String content();

    //

    /**
     * Annotation for use in {@link io.github.wasabithumb.jtoml.serial.TomlSerializable reflect serialization}.
     * Binds a {@link CommentPosition#PRE PRE} comment to the field,
     * which will be written out to the TOML document.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
    @Repeatable(MultiComment.Pre.class)
    @interface Pre {
        @NotNull @Pattern("^[^\\x00-\\x08\\x0A-\\x1F\\x7F]*$") String value();
    }

    /**
     * Annotation for use in {@link io.github.wasabithumb.jtoml.serial.TomlSerializable reflect serialization}.
     * Binds an {@link CommentPosition#INLINE INLINE} comment to the field,
     * which will be written out to the TOML document.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
    @Repeatable(MultiComment.Inline.class)
    @interface Inline {
        @NotNull @Pattern("^[^\\x00-\\x08\\x0A-\\x1F\\x7F]*$") String value();
    }

    /**
     * Annotation for use in {@link io.github.wasabithumb.jtoml.serial.TomlSerializable reflect serialization}.
     * Binds a {@link CommentPosition#POST POST} comment to the field,
     * which will be written out to the TOML document.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
    @Repeatable(MultiComment.Post.class)
    @interface Post {
        @NotNull @Pattern("^[^\\x00-\\x08\\x0A-\\x1F\\x7F]*$") String value();
    }

}
