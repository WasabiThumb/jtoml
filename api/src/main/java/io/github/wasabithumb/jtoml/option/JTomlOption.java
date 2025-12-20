package io.github.wasabithumb.jtoml.option;

import io.github.wasabithumb.jtoml.option.prop.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.time.ZoneOffset;

import static io.github.wasabithumb.jtoml.option.ObjectJTomlOption.of;
import static io.github.wasabithumb.jtoml.option.BooleanJTomlOption.of;

/**
 * An enum-like option key for JToml
 */
@ApiStatus.NonExtendable
public interface JTomlOption<T> {

    /**
     * Indentation to apply when writing
     */
    JTomlOption<IndentationPolicy> INDENTATION = of("INDENTATION", IndentationPolicy.class, IndentationPolicy.STANDARD);

    /**
     * Spacing to apply when writing
     */
    JTomlOption<SpacingPolicy> SPACING = of("SPACING", SpacingPolicy.class, SpacingPolicy.STANDARD);

    /**
     * Padding to apply when writing
     */
    JTomlOption<PaddingPolicy> PADDING = of("PADDING", PaddingPolicy.class, PaddingPolicy.STANDARD);

    /**
     * Zone offset to use when reading a <a href="https://toml.io/en/v1.1.0#local-date-time">Local Date-Time</a>
     * as an {@link java.time.OffsetDateTime OffsetDateTime}
     * and when writing a {@link java.time.LocalDateTime LocalDateTime} as an
     * <a href="https://toml.io/en/v1.1.0#offset-date-time">Offset Date-Time</a>
     */
    JTomlOption<ZoneOffset> TIME_ZONE = of("TIME_ZONE", ZoneOffset.class, ZoneOffset.UTC);

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
    JTomlOption<OrderMarkPolicy> READ_BOM = of("READ_BOM", OrderMarkPolicy.class, OrderMarkPolicy.NEVER);

    /**
     * Determines if a BOM should be written
     */
    JTomlOption<OrderMarkPolicy> WRITE_BOM = of("WRITE_BOM", OrderMarkPolicy.class, OrderMarkPolicy.IF_PRESENT);

    /**
     * The line separator to use when writing and normalizing,
     * either {@link LineSeparator#LF LF} or {@link LineSeparator#CRLF CRLF}.
     * Default is determined by {@link System#lineSeparator()}.
     * Both line endings can always be read, irrespective of the value of this option.
     */
    JTomlOption<LineSeparator> LINE_SEPARATOR = of("LINE_SEPARATOR", LineSeparator.class, LineSeparator.SYSTEM);

    /**
     * If true, {@link io.github.wasabithumb.jtoml.except.parse.TomlExtensionException static extension} is
     * prohibited. This is required for the parser to be fully TOML-compliant.
     */
    Bool EXTENSION_GUARD = of("EXTENSION_GUARD", true);

    /**
     * If true, table headers will be written even if they do not
     * contain any key-values or comments.
     */
    @ApiStatus.AvailableSince("0.2.3")
    Bool WRITE_EMPTY_TABLES = of("WRITE_EMPTY_TABLES", false);

    /**
     * If true, comments will be stored in the resulting document
     * (rather than ignored) when reading
     */
    @ApiStatus.AvailableSince("0.6.0")
    Bool READ_COMMENTS = of("READ_COMMENTS", true);

    /**
     * If true, comments defined on values will be written.
     */
    @ApiStatus.AvailableSince("0.6.0")
    Bool WRITE_COMMENTS = of("WRITE_COMMENTS", true);

    /**
     * Determines how non-table arrays should be written;
     * specifically when elements should receive a newline
     */
    @ApiStatus.AvailableSince("0.6.0")
    JTomlOption<ArrayStrategy> ARRAY_STRATEGY = of("ARRAY_STRATEGY", ArrayStrategy.class, ArrayStrategy.DYNAMIC);

    /**
     * Determines how keys are sorted within a table
     * when writing.
     */
    @ApiStatus.AvailableSince("1.3.0")
    JTomlOption<SortMethod> SORTING = of("SORTING", SortMethod.class, SortMethod.STRATIFIED);

    /**
     * Determines the version of the TOML spec
     * to adhere to. Defaults to
     * {@link SpecVersion#latest() the latest supported version}.
     */
    JTomlOption<SpecVersion> COMPLIANCE = of("COMPLIANCE", SpecVersion.class, SpecVersion.latest());

    //

    /**
     * Returns a new array containing all options.
     * The array is sorted from least to greatest {@link #ordinal() ordinal},
     * just as in {@link Class#getEnumConstants()}
     */
    @Contract("-> new")
    static @NotNull JTomlOption<?> @NotNull [] values() {
        final Field[] fields = JTomlOption.class.getDeclaredFields();
        JTomlOption<?>[] ret = new JTomlOption<?>[fields.length];
        int head = 0;

        // Iterate over all declared fields
        for (Field field : fields) {
            if (!JTomlOption.class.isAssignableFrom(field.getType())) continue;
            Object obj;
            try {
                obj = field.get(null);
            } catch (ReflectiveOperationException | SecurityException e) {
                throw new AssertionError("Failed to read field (" + field.getName() + ")", e);
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

    @ApiStatus.Internal
    int ordinal();

    @NotNull String name();

    @NotNull Class<T> valueClass();

    @NotNull T defaultValue();

    default boolean isLegal(@NotNull T value) {
        return true;
    }

    //

    @ApiStatus.NonExtendable
    interface Bool extends JTomlOption<Boolean> {

        @Override
        default @NotNull Class<Boolean> valueClass() {
            return Boolean.class;
        }

    }

}
