/*
 * Copyright 2026 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wasabithumb.jtoml.option;

import io.github.wasabithumb.jtoml.key.convention.KeyConvention;
import io.github.wasabithumb.jtoml.key.convention.StandardKeyConvention;
import io.github.wasabithumb.jtoml.option.prop.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Field;
import java.time.ZoneOffset;

import static io.github.wasabithumb.jtoml.option.ObjectJTomlOption.of;
import static io.github.wasabithumb.jtoml.option.BooleanJTomlOption.of;

/**
 * An enum-like option key for JToml.
 * Each option has a non-null default value.
 * To change an option's value, it must be
 * reflected in the {@link JTomlOptions}
 * provided when creating the
 * {@link io.github.wasabithumb.jtoml.JToml JToml}
 * instance.
 */
@ApiStatus.NonExtendable
public interface JTomlOption<T> {

    /**
     * Indentation to apply when writing.
     * This refers to whitespace placed at the
     * beginning of lines.
     * Defaults to {@link IndentationPolicy#STANDARD STANDARD}.
     * @see IndentationPolicy
     */
    JTomlOption<IndentationPolicy> INDENTATION = of(
            "INDENTATION",
            IndentationPolicy.class,
            IndentationPolicy.STANDARD
    );

    /**
     * Spacing to apply when writing.
     * This refers to newlines placed around
     * symbols.
     * Defaults to {@link SpacingPolicy#STANDARD STANDARD}.
     * @see SpacingPolicy
     */
    JTomlOption<SpacingPolicy> SPACING = of(
            "SPACING",
            SpacingPolicy.class,
            SpacingPolicy.STANDARD
    );

    /**
     * Padding to apply when writing.
     * This refers to whitespace placed
     * within symbols, such as between the
     * braces and key of a table header.
     * Defaults to {@link PaddingPolicy#STANDARD STANDARD}.
     * @see PaddingPolicy
     */
    JTomlOption<PaddingPolicy> PADDING = of(
            "PADDING",
            PaddingPolicy.class,
            PaddingPolicy.STANDARD
    );

    /**
     * Zone offset to use when reading a <a href="https://toml.io/en/v1.1.0#local-date-time">Local Date-Time</a>
     * as an {@link java.time.OffsetDateTime OffsetDateTime}
     * and when writing a {@link java.time.LocalDateTime LocalDateTime} as an
     * <a href="https://toml.io/en/v1.1.0#offset-date-time">Offset Date-Time</a>.
     * Since a zone is being demanded where it is not specified, JToml looks to this option to select one.
     * Defaults to {@link ZoneOffset#UTC} for consistent behavior.
     * To respect the local time zone, set this to {@link ZoneOffset#systemDefault()}
     * (or don't perform any such ambiguous operation in the first place).
     */
    JTomlOption<ZoneOffset> TIME_ZONE = of(
            "TIME_ZONE",
            ZoneOffset.class,
            ZoneOffset.UTC
    );

    /**
     * Determines if a BOM should be read.
     * Per <a href="https://datatracker.ietf.org/doc/html/rfc3629">RFC 3629</a>:
     * <pre>
     * A protocol SHOULD forbid use of U+FEFF as a signature for those
     * textual protocol elements that the protocol mandates to be always
     * UTF-8, the signature function being totally useless in those
     * cases.
     * </pre>
     * Hence, the default is {@link OrderMarkPolicy#NEVER NEVER}.
     */
    JTomlOption<OrderMarkPolicy> READ_BOM = of(
            "READ_BOM",
            OrderMarkPolicy.class,
            OrderMarkPolicy.NEVER
    );

    /**
     * Determines if a BOM should be written.
     * Defaults to {@link OrderMarkPolicy#IF_PRESENT IF_PRESENT}
     * which typically does nothing unless {@link #READ_BOM} is also
     * configured.
     */
    JTomlOption<OrderMarkPolicy> WRITE_BOM = of(
            "WRITE_BOM",
            OrderMarkPolicy.class,
            OrderMarkPolicy.IF_PRESENT
    );

    /**
     * The line separator to use when writing and normalizing,
     * either {@link LineSeparator#LF LF} or {@link LineSeparator#CRLF CRLF}.
     * Defaults to {@link LineSeparator#SYSTEM SYSTEM}.
     * Both line endings can always be read, irrespective of the value of this option.
     */
    JTomlOption<LineSeparator> LINE_SEPARATOR = of(
            "LINE_SEPARATOR",
            LineSeparator.class,
            LineSeparator.SYSTEM
    );

    /**
     * If {@code true}, {@link io.github.wasabithumb.jtoml.except.parse.TomlExtensionException static extension} is
     * prohibited. This is required for the parser to be fully TOML-compliant.
     * Defaults to {@code true}.
     */
    Bool EXTENSION_GUARD = of(
            "EXTENSION_GUARD",
            true
    );

    /**
     * If {@code true}, table headers will be written even if they do not
     * contain any key-values or comments.
     * Defaults to {@code false}.
     */
    @ApiStatus.AvailableSince("0.2.3")
    Bool WRITE_EMPTY_TABLES = of(
            "WRITE_EMPTY_TABLES",
            false
    );

    /**
     * If {@code true}, comments will be stored in the resulting document
     * (rather than ignored) when reading. When {@code false}, creation of
     * {@link io.github.wasabithumb.jtoml.comment.Comments Comments} objects is
     * not prevented, however such objects within read documents will be empty.
     * Defaults to {@code true}.
     */
    @ApiStatus.AvailableSince("0.6.0")
    Bool READ_COMMENTS = of(
            "READ_COMMENTS",
            true
    );

    /**
     * If {@code true}, comments defined on values will be written.
     * Defaults to {@code true}.
     */
    @ApiStatus.AvailableSince("0.6.0")
    Bool WRITE_COMMENTS = of(
            "WRITE_COMMENTS",
            true
    );

    /**
     * Determines how non-table arrays should be written;
     * specifically when elements should receive a newline.
     * Defaults to {@link ArrayStrategy#DYNAMIC DYNAMIC}.
     */
    @ApiStatus.AvailableSince("0.6.0")
    JTomlOption<ArrayStrategy> ARRAY_STRATEGY = of(
            "ARRAY_STRATEGY",
            ArrayStrategy.class,
            ArrayStrategy.DYNAMIC
    );

    /**
     * Determines the order of keys within a block
     * when writing. Defaults to {@link SortMethod#STRATIFIED STRATIFIED}.
     */
    @ApiStatus.AvailableSince("1.3.0")
    JTomlOption<SortMethod> SORTING = of(
            "SORTING",
            SortMethod.class,
            SortMethod.STRATIFIED
    );

    /**
     * Determines the version of the TOML spec
     * to adhere to. Defaults to
     * {@link SpecVersion#latest() the latest supported version}.
     */
    @ApiStatus.AvailableSince("1.4.0")
    JTomlOption<SpecVersion> COMPLIANCE = of(
            "COMPLIANCE",
            SpecVersion.class,
            SpecVersion.latest()
    );

    /**
     * Determines the default {@link KeyConvention} to use
     * during reflect serialization. This determines how
     * a field/component declared on a serialized class/record should
     * be mapped to a TOML key unless otherwise specified
     * with the {@code @Key} or {@code @Convention} annotations
     * at the type or member level. Defaults to
     * {@link StandardKeyConvention#LITERAL LITERAL}.
     */
    @ApiStatus.AvailableSince("1.5.0")
    JTomlOption<KeyConvention> DEFAULT_KEY_CONVENTION = of(
            "DEFAULT_KEY_CONVENTION",
            KeyConvention.class,
            StandardKeyConvention.LITERAL
    );

    /**
     * If {@code true}, POJOs (non-records) handled
     * by the reflect serializer are not required to implement
     * {@link io.github.wasabithumb.jtoml.serial.TomlSerializable TomlSerializable}.
     * This also disables the protection implemented in the
     * reflect serializer which prevents fields in supertypes
     * which do not implement the marker from being modified.
     * Defaults to {@code false}.
     */
    @ApiStatus.AvailableSince("1.6.0")
    Bool IGNORE_SERIALIZABLE_MARKER = of(
            "IGNORE_SERIALIZABLE_MARKER",
            false
    );

    /**
     * If {@code true}, permits the usage of {@code sun.misc.Unsafe} to
     * instantiate classes without a no-args constructor.
     * Defaults to {@code false}.
     */
    @ApiStatus.AvailableSince("1.6.0")
    Bool PERMIT_UNSAFE = of(
            "PERMIT_UNSAFE",
            false
    );

    /**
     * If {@code true} (defaults to {@code false}), parsing errors will be
     * {@link io.github.wasabithumb.jtoml.document.TomlDocument#issues() stored on the resulting document}
     * instead of being thrown. {@link io.github.wasabithumb.jtoml.except.TomlIOException TomlIOException}
     * may still be thrown when the underlying stream raises an {@code IOException}.
     */
    @ApiStatus.AvailableSince("1.7.0")
    Bool ERROR_RECOVERY = of(
            "ERROR_RECOVERY",
            false
    );

    //

    /**
     * Returns a new array containing all options.
     * The array is sorted from least to greatest {@link #ordinal() ordinal},
     * just as in {@link Class#getEnumConstants()}
     */
    @Contract("-> new")
    static JTomlOption<?>[] values() {
        final Field[] fields = JTomlOption.class.getDeclaredFields();
        JTomlOption<?>[] ret = new JTomlOption<?>[fields.length];
        int head = 0;

        // Iterate over all declared fields
        for (Field field : fields) {
            if (!JTomlOption.class.isAssignableFrom(field.getType())) continue;
            Object obj;
            try {
                obj = field.get(null);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to read interface field (" + field.getName() + ")", e);
            }
            ret[head++] = (JTomlOption<?>) obj;
        }

        // If there are less option constants than fields, shrink the array
        // This shouldn't happen, but it's not worth breaking the project
        // when some compiler decides to add a synthetic field
        if (head < fields.length) {
            JTomlOption<?>[] cpy = new JTomlOption<?>[head];
            System.arraycopy(ret, 0, cpy, 0, head);
            ret = cpy;
        }

        // Sort by ordinal: in the case that the array is already sorted (highly likely),
        // this sort completes as fast as possible. Standard sort would also probably be fine.
        final int maxIndex = head - 1;
        JTomlOption<?> tmp;
        for (int z=0; z < maxIndex; z++) {
            tmp = ret[z];
            if (tmp.ordinal() == z) continue;
            int t = z;
            while ((++t) < maxIndex) {
                if (ret[t].ordinal() == z) break;
            }
            ret[z] = ret[t];
            ret[t] = tmp;
        }

        return ret;
    }

    //

    /**
     * Reports an arbitrary positive integer value
     * which is unique to this option constant,
     * exactly like {@link Enum#ordinal() Enum#ordinal}.
     * @apiNote This is considered stable API, but you probably have no good reason to use it.
     */
    @ApiStatus.Internal
    int ordinal();

    /**
     * Reports a name for this option constant, exactly equal to
     * the name of the field in the {@link JTomlOption} class
     * with the same value as this object by convention.
     */
    String name();

    /**
     * Reports the type that values associated with this option
     * constant must be an instance of.
     */
    Class<T> valueClass();

    /**
     * Reports the default value associated with this option
     * constant.
     */
    T defaultValue();

    /**
     * Returns true if the given instance of {@link #valueClass() the value class}
     * can be associated with this option. This exists so that
     * options can place restrictions on option values finer than
     * their type.
     * @param value The value to check
     * @return True if the value is legal for this option
     * @apiNote Currently always returns true
     */
    default boolean isLegal(T value) {
        return true;
    }

    //

    /**
     * An additional marker interface implemented by
     * {@link JTomlOption} implementation(s) when their {@link JTomlOption#valueClass() value class}
     * is known to be {@code Boolean.class}, increasing API flexibility.
     */
    @ApiStatus.NonExtendable
    interface Bool extends JTomlOption<Boolean> {

        @Override
        default Class<Boolean> valueClass() {
            return Boolean.class;
        }

    }

}
