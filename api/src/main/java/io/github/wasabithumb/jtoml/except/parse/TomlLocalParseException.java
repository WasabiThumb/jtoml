package io.github.wasabithumb.jtoml.except.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link TomlParseException} with location info
 */
public final class TomlLocalParseException extends TomlParseException {

    private final int lineNumber;
    private final int columnNumber;

    public TomlLocalParseException(@NotNull String message, int lineNumber, int columnNumber) {
        super(message);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public TomlLocalParseException(@NotNull String message, @Nullable Throwable cause, int lineNumber, int columnNumber) {
        super(message, cause);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    //

    public @NotNull String getRawMessage() {
        return super.getMessage();
    }

    @Override
    public @NotNull String getMessage() {
        return super.getMessage() + " @ " + this.lineNumber + ":" + this.columnNumber;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getColumnNumber() {
        return this.columnNumber;
    }

}
