package io.github.wasabithumb.jtoml.except.parse;

import org.jetbrains.annotations.NotNull;

/**
 * An issue was found with the BOM in the source data.
 * <ul>
 *     <li>
 *         {@link io.github.wasabithumb.jtoml.option.JTomlOption#READ_BOM READ_BOM} is set to
 *         {@link io.github.wasabithumb.jtoml.option.prop.OrderMarkPolicy#NEVER NEVER} and a
 *         BOM is present
 *     </li>
 *     <li>
 *         {@link io.github.wasabithumb.jtoml.option.JTomlOption#READ_BOM READ_BOM} is set to
 *         {@link io.github.wasabithumb.jtoml.option.prop.OrderMarkPolicy#NEVER ALWAYS} and a
 *         BOM is not present
 *     </li>
 * </ul>
 */
public final class TomlBomException extends TomlParseException {

    public TomlBomException(@NotNull String message) {
        super(message);
    }

}
