package io.github.wasabithumb.jtoml.comment;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * <p>
 *     Holds annotations for applying multiple comments
 *     to values on {@link io.github.wasabithumb.jtoml.serial.TomlSerializable TomlSerializable}
 *     classes and records, for use with the reflect serializer
 *     ({@code jtoml-serializer-reflect}).
 * </p>
 * <p>
 *     It should not be necessary to use this directly; the compiler
 *     should generate these annotations whenever multiple single comment
 *     annotations are present on a field.
 * </p>
 * <table>
 *     <tr>
 *         <th>Comment Class</th>
 *         <th>Multi Comment Class</th>
 *     </tr>
 *     <tr>
 *         <td>{@link Comment.Pre @Comment.Pre}</td>
 *         <td>{@link Pre @MulitComment.Pre}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link Comment.Inline @Comment.Inline}</td>
 *         <td>{@link Inline @MulitComment.Inline}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link Comment.Post @Comment.Post}</td>
 *         <td>{@link Post @MulitComment.Post}</td>
 *     </tr>
 * </table>
 */
@ApiStatus.Internal
public abstract class MultiComment {

    private MultiComment() { }

    //

    /**
     * Variadic counterpart to {@link Comment.Pre @Comment.Pre}
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
    public @interface Pre {
        @NotNull Comment.Pre @NotNull [] value();
    }

    /**
     * Variadic counterpart to {@link Comment.Inline @Comment.Inline}
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
    public @interface Inline {
        @NotNull Comment.Inline @NotNull [] value();
    }

    /**
     * Variadic counterpart to {@link Comment.Post @Comment.Post}
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
    public @interface Post {
        @NotNull Comment.Post @NotNull [] value();
    }

}
