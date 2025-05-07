package io.github.wasabithumb.jtoml.except.parse;

import org.jetbrains.annotations.NotNull;

/**
 * An attempt was made to extend an inline table or array.
 * From the TOML docs:
 * <ul>
 *     <li>
 *         Inline tables are fully self-contained and define all keys and sub-tables within them.
 *         Keys and sub-tables cannot be added outside the braces.
 *     </li>
 *     <li>
 *         Attempting to append to a statically defined array, even if that array is empty,
 *         must produce an error at parse time.
 *     </li>
 * </ul>
 */
public final class TomlExtensionException extends TomlParseException {

    public TomlExtensionException(@NotNull String message) {
        super(message);
    }

}
