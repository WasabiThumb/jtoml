package io.github.wasabithumb.jtoml.comment;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the part of a TOML expression which a comment is bound to.
 * One of {@link #PRE}, {@link #POST} pr {@link #INLINE}.
 */
@ApiStatus.AvailableSince("0.6.0")
public enum CommentPosition {
    /**
     * Comment is bound to the line(s) directly
     * before the target expression.
     */
    PRE,

    /**
     * Comment is placed on the same line as
     * the target expression.
     */
    INLINE,

    /**
     * Comment is bound to the line(s) directly
     * after the target expression.
     */
    POST
}
