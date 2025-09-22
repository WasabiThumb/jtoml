package io.github.wasabithumb.jtoml.configurate.hint;

import io.github.wasabithumb.jtoml.comment.CommentPosition;
import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
public final class CommentForm {

    public static final CommentForm DEFAULT = new CommentForm(Integer.MAX_VALUE, false);

    //

    private final int pre;
    private final boolean inline;

    private CommentForm(int pre, boolean inline) {
        this.pre = pre;
        this.inline = inline;
    }

    public CommentForm(@NotNull Comments comments) {
        this(comments.get(CommentPosition.PRE).size(), comments.getInline() != null);
    }

    //

    public void apply(@NotNull String @NotNull [] src, @NotNull Comments dest) {
        String line;
        for (int i = 0; i < src.length; i++) {
            line = src[i];
            if (i < this.pre) {
                dest.addPre(line);
            } else if (i == this.pre && this.inline) {
                dest.addInline(line);
            } else {
                dest.addPost(line);
            }
        }
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
