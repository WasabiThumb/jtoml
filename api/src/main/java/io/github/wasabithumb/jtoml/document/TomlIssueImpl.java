package io.github.wasabithumb.jtoml.document;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class TomlIssueImpl implements TomlIssue {

    private final int line;
    private final int column;
    private final String message;

    TomlIssueImpl(
            int line,
            int column,
            @NotNull String message
    ) {
        this.line = line;
        this.column = column;
        this.message = message;
    }

    //

    @Override
    public int line() {
        return this.line;
    }

    @Override
    public int column() {
        return this.column;
    }

    @Override
    public @NotNull String message() {
        return this.message;
    }

    @Override
    public int hashCode() {
        int h = 7;
        h = 31 * h + this.line;
        h = 31 * h + this.column;
        h = 31 * h + this.message.hashCode();
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TomlIssue)) return false;
        TomlIssue other = (TomlIssue) obj;
        return this.line == other.line() &&
                this.column == other.column() &&
                this.message.equals(other.message());
    }

    @Override
    public String toString() {
        return "TomlIssue{line=" + this.line +
                ", column=" + this.column +
                ", message=" + this.message +
                "}";
    }

}
