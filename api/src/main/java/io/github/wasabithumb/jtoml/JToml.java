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
     * Creates a new JToml instance
     * @param options Options to set on the instance
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

    /**
     * Reads a TOML table from a string
     * @param toml A string containing a TOML document
     * @throws TomlException String is not valid TOML
     */
    @NotNull TomlDocument readFromString(@NotNull String toml) throws TomlException;

    /**
     * Reads a TOML table from a stream
     * @param in Stream to read from
     * @throws TomlIOException The underlying stream raised an exception
     * @throws TomlException Data is not valid TOML
     */
    @NotNull TomlDocument read(@NotNull InputStream in) throws TomlException;

    /**
     * Reads a TOML table from a reader. A reader that is configured to use
     * any encoding other than {@link java.nio.charset.StandardCharsets#UTF_8 UTF-8}
     * may fail to read some valid TOML documents, so this method should be used with care.
     * @param reader Reader to read from
     * @throws TomlIOException The underlying reader raised an exception
     * @throws TomlException Data is not valid TOML
     * @see #read(InputStream)
     */
    @ApiStatus.AvailableSince("0.3.0")
    @NotNull TomlDocument read(@NotNull Reader reader) throws TomlException;

    /**
     * Reads a TOML table from the filesystem
     * @param file Path to the TOML file
     * @throws TomlIOException The filesystem raised an exception
     * @throws TomlException File is not valid TOML
     * @see #read(InputStream)
     */
    default @NotNull TomlDocument read(@NotNull Path file) throws TomlException {
        try (InputStream is = Files.newInputStream(file, StandardOpenOption.READ)) {
            return this.read(is);
        } catch (IOException e) {
            TomlIOException.rethrow(e);
            return null;
        }
    }

    /**
     * Reads a TOML table from the filesystem
     * @param file Path to the TOML file
     * @throws TomlIOException The filesystem raised an exception
     * @throws TomlException File is not valid TOML
     * @see #read(InputStream)
     */
    default @NotNull TomlDocument read(@NotNull File file) throws TomlException {
        return this.read(file.toPath());
    }

    //

    /**
     * Writes a TOML table to a string
     * @param table Table to write
     * @throws TomlValueException The TOML spec does not allow for lossless serialization of the table data.
     * See exception docs for more details.
     */
    @NotNull String writeToString(@NotNull TomlTable table) throws TomlValueException;

    /**
     * Writes a TOML table to a stream
     * @param out Stream to receive the UTF-8 encoded TOML data
     * @param table Table to write
     * @throws TomlValueException The TOML spec does not allow for lossless serialization of the table data.
     * See exception docs for more details.
     * @throws TomlIOException The underlying stream raised an exception
     */
    void write(@NotNull OutputStream out, @NotNull TomlTable table) throws TomlValueException, TomlIOException;

    /**
     * Writes a TOML table to a writer. If the writer is configured to use any encoding other than
     * {@link java.nio.charset.StandardCharsets#UTF_8 UTF-8}, this may produce documents which are not
     * considered valid by the TOML spec. Hence, this method should be used with care.
     * @param writer Writer to receive the TOML plaintext
     * @param table Table to write
     * @throws TomlValueException The TOML spec does not allow for lossless serialization of the table data.
     * See exception docs for more details.
     * @throws TomlIOException The underlying writer raised an exception
     */
    @ApiStatus.AvailableSince("0.3.0")
    void write(@NotNull Writer writer, @NotNull TomlTable table) throws TomlValueException, TomlIOException;

    /**
     * Writes a TOML table to a file
     * @param file Path of the file to receive the UTF-8 encoded TOML data
     * @param table Table to write
     * @throws TomlValueException The TOML spec does not allow for lossless serialization of the table data.
     * See exception docs for more details.
     * @throws TomlIOException The underlying filesystem raised an exception
     * @see #write(OutputStream, TomlTable)
     */
    default void write(@NotNull Path file, @NotNull TomlTable table) throws TomlValueException, TomlIOException {
        try (OutputStream os = Files.newOutputStream(
                file,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )) {
            this.write(os, table);
        } catch (IOException e) {
            TomlIOException.rethrow(e);
        }
    }

    /**
     * Writes a TOML table to a file
     * @param file Path of the file to receive the UTF-8 encoded TOML data
     * @param table Table to write
     * @throws TomlValueException The TOML spec does not allow for lossless serialization of the table data.
     * See exception docs for more details.
     * @throws TomlIOException The underlying filesystem raised an exception
     * @see #write(OutputStream, TomlTable)
     */
    default void write(@NotNull File file, @NotNull TomlTable table) throws TomlValueException, TomlIOException {
        this.write(file.toPath(), table);
    }

    //

    /**
     * Serializes the given TOML table to the given type,
     * if an appropriate serializer is present in the classpath.
     * A list of serializers can be found {@link io.github.wasabithumb.jtoml.serial.TomlSerializer here}.
     * @param type The type to serialize to
     * @param table The table to serialize
     * @throws IllegalArgumentException No serializer is registered for the given type
     * @deprecated Use {@link #fromToml(Class, TomlTable)}
     */
    @Deprecated
    default <T> @NotNull T serialize(@NotNull Class<T> type, @NotNull TomlTable table) throws IllegalArgumentException {
        return this.fromToml(type, table);
    }

    /**
     * Converts the given TOML table to the given type,
     * if an appropriate serializer is present in the classpath.
     * A list of serializers can be found {@link io.github.wasabithumb.jtoml.serial.TomlSerializer here}.
     * @param type The type to convert to
     * @param table The table to convert
     * @throws IllegalArgumentException No serializer is registered for the given type
     */
    @ApiStatus.AvailableSince("1.2.1")
    <T> @NotNull T fromToml(@NotNull Class<T> type, @NotNull TomlTable table) throws IllegalArgumentException;

    /**
     * Deserializes a given TOML table from the given type,
     * if an appropriate deserializer is present in the classpath.
     * A list of serializers can be found {@link io.github.wasabithumb.jtoml.serial.TomlSerializer here}.
     * @param type The type to deserialize from
     * @param data The data to deserialize into a TOML table
     * @throws IllegalArgumentException No deserializer is registered for the given type
     * @deprecated Use {@link #toToml(Class, Object)}
     */
    @Deprecated
    default <T> @NotNull TomlTable deserialize(@NotNull Class<T> type, @NotNull T data) throws IllegalArgumentException {
        return this.toToml(type, data);
    }

    /**
     * Converts the given type to a TOML table,
     * if an appropriate deserializer is present in the classpath.
     * A list of serializers can be found {@link io.github.wasabithumb.jtoml.serial.TomlSerializer here}.
     * @param type The type to convert from
     * @param data The data to convert into a TOML table
     * @throws IllegalArgumentException No deserializer is registered for the given type
     */
    @ApiStatus.AvailableSince("1.2.1")
    <T> @NotNull TomlTable toToml(@NotNull Class<T> type, @NotNull T data) throws IllegalArgumentException;

    /**
     * Converts the given object to a TOML table.
     * Defers to {@link #toToml(Class, Object)} internally.
     * @param data The object to convert
     * @throws IllegalArgumentException No known deserializer can handle objects of the given type
     */
    @NotNull TomlTable toToml(@NotNull Object data) throws IllegalArgumentException;

}
