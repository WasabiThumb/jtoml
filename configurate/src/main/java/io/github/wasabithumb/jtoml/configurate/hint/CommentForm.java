package io.github.wasabithumb.jtoml.configurate.hint;

import io.github.wasabithumb.jtoml.comment.CommentPosition;
import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
public final class CommentForm {

    private final int pre;
    private final boolean inline;

    public CommentForm(@NotNull Comments comments) {
        this.pre = comments.get(CommentPosition.PRE).size();
        this.inline = comments.getInline() != null;
    }

    //

    public int pre() {
        return this.pre;
    }

    public boolean inline() {
        return this.inline;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pre, this.inline);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommentForm)) return false;
        CommentForm other = (CommentForm) obj;
        return this.pre == other.pre &&
                this.inline == other.inline;
    }

}
