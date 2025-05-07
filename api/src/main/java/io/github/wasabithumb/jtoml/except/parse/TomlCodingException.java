package io.github.wasabithumb.jtoml.except.parse;

import org.jetbrains.annotations.NotNull;

/**
 * Error while decoding UTF-8 data.
 * This is similar to {@link java.nio.charset.MalformedInputException MalformedInputException}
 * and by extension {@link java.nio.charset.CharacterCodingException CharacterCodingException}.
 */
public final class TomlCodingException extends TomlParseException {

    public TomlCodingException(@NotNull String message) {
        super(message);
    }

}
