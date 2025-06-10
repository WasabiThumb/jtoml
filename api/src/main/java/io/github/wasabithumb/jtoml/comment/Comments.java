package io.github.wasabithumb.jtoml.comment;

import org.jetbrains.annotations.*;

import java.util.List;

/**
 * Holds a mutable collection of comments bound to a
 * specific TOML expression
 */
@ApiStatus.NonExtendable
public interface Comments {

    @Contract("-> new")
    static @NotNull Comments empty() {
        return new CommentsImpl();
    }

    //

    /**
     * Provides an immutable list of all comments held
     * by this object, in the order they would appear in a
     * document. Comments are sorted by {@link CommentPosition position},
     * then by insertion order.
     */
    @NotNull @Unmodifiable List<Comment> all();

    /**
     * Provides an immutable list of all comments of the given
     * {@link CommentPosition position} in insertion order.
     */
    @NotNull @Unmodifiable List<Comment> get(@NotNull CommentPosition position);

    /**
     * Gets the content of the {@link CommentPosition#INLINE INLINE} comment,
     * if any is set.
     */
    default @Nullable String getInline() {
        List<Comment> list = this.get(CommentPosition.INLINE);
        if (list.isEmpty()) return null;
        return list.get(0).content();
    }

    /**
     * Removes all comments
     */
    void clear();

    /**
     * Removes all comments of the given {@link CommentPosition position}.
     */
    void clear(@NotNull CommentPosition position);

    /**
     * Adds a comment. If the position of the comment is
     * {@link CommentPosition#INLINE INLINE}, any existing
     * inline comment is replaced.
     */
    void add(@NotNull Comment comment);

    /**
     * Creates and adds a comment
     * @param position Position of the comment. If {@link CommentPosition#INLINE INLINE},
     *                 any existing inline comment is replaced.
     * @param content Content of the comment
     * @see #add(Comment)
     */
    default void add(@NotNull CommentPosition position, @NotNull String content) {
        this.add(Comment.of(position, content));
    }

    /**
     * Alias for {@code add(CommentPosition.PRE, content)}
     * @see #add(CommentPosition, String)
     */
    default void addPre(@NotNull String content) {
        this.add(CommentPosition.PRE, content);
    }

    /**
     * Alias for {@code add(CommentPosition.INLINE, content)}
     * @see #add(CommentPosition, String)
     */
    default void addInline(@NotNull String content) {
        this.add(CommentPosition.INLINE, content);
    }

    /**
     * Sets the content of the inline comment, or clears it
     * if null
     */
    default void setInline(@Nullable String content) {
        if (content == null) {
            this.clear(CommentPosition.INLINE);
        } else {
            this.add(CommentPosition.INLINE, content);
        }
    }

    /**
     * Alias for {@code add(CommentPosition.POST, content)}
     * @see #add(CommentPosition, String)
     */
    default void addPost(@NotNull String content) {
        this.add(CommentPosition.POST, content);
    }

}
