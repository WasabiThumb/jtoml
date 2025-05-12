package io.github.wasabithumb.jtoml;

import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import io.github.wasabithumb.jtoml.except.TomlValueException;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Entry point for JToml
 * @see #jToml()
 * @see #jToml(JTomlOptions)
 */
@ApiStatus.NonExtendable
public interface JToml {

    /**
     * Creates a new JToml instance with the given options
     * @see JTomlOptions#builder()
     */
    @Contract("_ -> new")
    static @NotNull JToml jToml(@NotNull JTomlOptions options) {
        return JTomlService.get().createInstance(options);
    }

    /**
     * Provides the default JToml instance
     */
    @Contract(pure = true)
    static @NotNull JToml jToml() {
        return JTomlService.get().defaultInstance();
    }

    //

    /** Reads a TOML table from a string */
    @NotNull TomlDocument readFromString(@NotNull String toml) throws TomlException;

    /** Reads a TOML table from a stream */
    @NotNull TomlDocument read(@NotNull InputStream in) throws TomlException;

    /** Reads a TOML table from a reader */
    @ApiStatus.AvailableSince("0.3.0")
    @NotNull TomlDocument read(@NotNull Reader reader) throws TomlException;

    /** Reads a TOML table from a file */
    default @NotNull TomlDocument read(@NotNull Path file) throws TomlException {
        try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
            return this.read(is);
        } catch (IOException e) {
            TomlIOException.rethrow(e);
            return null;
        }
    }

    /** Reads a TOML table from a file */
    default @NotNull TomlDocument read(@NotNull File file) throws TomlException {
        return this.read(file.toPath());
    }

    //

    /** Converts a TOML table to a string */
    @NotNull String writeToString(@NotNull TomlTable table) throws TomlValueException;

    /** Writes a TOML table to a stream */
    void write(@NotNull OutputStream out, @NotNull TomlTable table) throws TomlException;

    /** Writes a TOML table to a writer */
    @ApiStatus.AvailableSince("0.3.0")
    void write(@NotNull Writer writer, @NotNull TomlTable table) throws TomlException;

    /** Writes a TOML table to a file */
    default void write(@NotNull Path file, @NotNull TomlTable table) throws TomlException {
        try (OutputStream os = Files.newOutputStream(
                file,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )) {
            this.write(os, table);
        } catch (IOException e) {
            TomlIOException.rethrow(e);
        }
    }

    /** Writes a TOML table to a file */
    default void write(@NotNull File file, @NotNull TomlTable table) throws TomlException {
        this.write(file.toPath(), table);
    }

    //

    /**
     * Serializes the given TOML table to the given type,
     * if an appropriate serializer is present in the classpath
     */
    <T> @NotNull T serialize(@NotNull Class<T> type, @NotNull TomlTable table) throws IllegalArgumentException;

    /**
     * Deserializes a TOML table from the given type,
     * if an appropriate deserializer is present in the classpath
     */
    <T> @NotNull TomlTable deserialize(@NotNull Class<T> type, @NotNull T data) throws IllegalArgumentException;

}
