package io.github.wasabithumb.jtoml.comment;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
final class CommentImpl implements Comment {

    private static void checkForInvalidChars(@NotNull String content) {
        final int len = content.length();
        char c;
        for (int i=0; i < len; i++) {
            c = content.charAt(i);
            if (c >= 0x20) continue;
            if (c == '\t') continue;
            throw new IllegalArgumentException(
                    "Illegal character \\x" +
                            (c >= 0x10 ? '1' : '0') +
                            Character.forDigit(c & 0xF, 16) +
                            " @ offset " + i
            );
        }
    }

    //

    private final CommentPosition position;
    private final String content;

    CommentImpl(@NotNull CommentPosition position, @NotNull String content) {
        checkForInvalidChars(content);
        this.position = position;
        this.content = content;
    }

    //

    @Override
    public @NotNull CommentPosition position() {
        return this.position;
    }

    @Override
    public @NotNull String content() {
        return this.content;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.position, this.content);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommentImpl)) return false;
        CommentImpl other = (CommentImpl) obj;
        return this.position == other.position &&
                this.content.equals(other.content);
    }

    @Override
    public @NotNull String toString() {
        return "Comment[position=" + this.position.name() + ", content=" + this.content + "]";
    }

}
