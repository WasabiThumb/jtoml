package io.github.wasabithumb.jtoml.document;

import io.github.wasabithumb.jtoml.except.parse.TomlLocalParseException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A silenced parsing error stored on a {@link TomlDocument document}
 * when {@link io.github.wasabithumb.jtoml.option.JTomlOption#ERROR_RECOVERY error recovery}
 * is enabled.
 */
@ApiStatus.NonExtendable
public interface TomlIssue {

    /**
     * Creates an issue wrapping the given
     * exception.
     */
    @Contract("null -> fail; !null -> new")
    static TomlIssue issue(TomlLocalParseException exception) {
        Objects.requireNonNull(exception, "exception must not be null");
        return new TomlIssueImpl(exception.getLineNumber(), exception.getColumnNumber(), exception.getRawMessage());
    }

    /**
     * Creates a new issue.
     * @param line Line number of the issue (starting from 0)
     * @param column Column number of the issue (starting from 0)
     * @param message Message string of the issue
     */
    @Contract("_, _, null -> fail; _, _, !null -> new")
    static TomlIssue issue(int line, int column, String message) {
        if (line < 0) throw new IllegalStateException("line must not be negative");
        if (column < 0) throw new IllegalStateException("column must not be negative");
        return new TomlIssueImpl(
                line,
                column,
                Objects.requireNonNull(message, "message must not be null")
        );
    }

    //

    @Contract(pure = true)
    int line();

    @Contract(pure = true)
    int column();

    @Contract(pure = true)
    @NotNull String message();

}
