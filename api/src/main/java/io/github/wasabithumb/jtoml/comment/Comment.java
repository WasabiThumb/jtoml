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
    @interface Pre {
        @NotNull @Pattern("^[\\x09\\x20-\\uFFFF]*$") String value();
    }

    /**
     * Annotation for use in {@link io.github.wasabithumb.jtoml.serial.TomlSerializable reflect serialization}.
     * Binds an {@link CommentPosition#INLINE INLINE} comment to the field,
     * which will be written out to the TOML document.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
    @interface Inline {
        @NotNull @Pattern("^[\\x09\\x20-\\uFFFF]*$") String value();
    }

    /**
     * Annotation for use in {@link io.github.wasabithumb.jtoml.serial.TomlSerializable reflect serialization}.
     * Binds a {@link CommentPosition#POST POST} comment to the field,
     * which will be written out to the TOML document.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
    @interface Post {
        @NotNull @Pattern("^[\\x09\\x20-\\uFFFF]*$") String value();
    }

}
