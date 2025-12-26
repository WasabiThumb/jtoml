package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.function.Function;

/**
 * Handles conversion of a single Java type to/from
 * a TOML document leaf.
 * @see #of(Class, Function, Function)
 * @see #BOOL
 * @see #BYTE
 * @see #SHORT
 * @see #INT
 * @see #LONG
 * @see #FLOAT
 * @see #DOUBLE
 * @see #CHAR
 * @see #STRING
 * @see #OFFSET_DATE_TIME
 * @see #LOCAL_DATE_TIME
 * @see #LOCAL_DATE
 * @see #LOCAL_TIME
 */
public interface TypeAdapter<T> {

    /**
     * Helper function to create a new {@link TypeAdapter}.
     * @param typeClass The Java type to adapt to/from a TOML value.
     * @param toJava Function invoked to convert a TOML value to an instance of the given type.
     * @param toToml Function invoked to convert an instance of the given type to a TOML value.
     */
    @Contract("_, _, _ -> new")
    static <O> @NotNull TypeAdapter<O> of(
            @NotNull Class<O> typeClass,
            @NotNull Function<TomlValue, O> toJava,
            @NotNull Function<O, TomlValue> toToml
    ) {
        return new TypeAdapterImpl<>(typeClass, toJava, toToml);
    }

    //

    /**
     * Adapts TOML booleans to/from Java {@code boolean}/{@link Boolean} fields.
     */
    TypeAdapter<Boolean> BOOL = of(
            Boolean.TYPE,
            (TomlValue tv) -> tv.asPrimitive().asBoolean(),
            TomlPrimitive::of
    );

    /**
     * Adapts TOML integers to/from Java {@code byte}/{@link Byte} fields.
     */
    TypeAdapter<Byte> BYTE = of(
            Byte.TYPE,
            (TomlValue tv) -> (byte) tv.asPrimitive().asInteger(),
            (Byte b) -> TomlPrimitive.of(b.intValue())
    );

    /**
     * Adapts TOML integers to/from Java {@code short}/{@link Short} fields.
     */
    TypeAdapter<Short> SHORT = of(
            Short.TYPE,
            (TomlValue tv) -> (short) tv.asPrimitive().asInteger(),
            (Short s) -> TomlPrimitive.of(s.intValue())
    );

    /**
     * Adapts TOML integers to/from Java {@code int}/{@link Integer} fields.
     */
    TypeAdapter<Integer> INT = of(
            Integer.TYPE,
            (TomlValue tv) -> tv.asPrimitive().asInteger(),
            TomlPrimitive::of
    );

    /**
     * Adapts TOML integers to/from Java {@code long}/{@link Long} fields.
     */
    TypeAdapter<Long> LONG = of(
            Long.TYPE,
            (TomlValue tv) -> tv.asPrimitive().asLong(),
            TomlPrimitive::of
    );

    /**
     * Adapts TOML floats to/from Java {@code float}/{@link Float} fields.
     */
    TypeAdapter<Float> FLOAT = of(
            Float.TYPE,
            (TomlValue tv) -> tv.asPrimitive().asFloat(),
            TomlPrimitive::of
    );

    /**
     * Adapts TOML floats to/from Java {@code double}/{@link Double} fields.
     */
    TypeAdapter<Double> DOUBLE = of(
            Double.TYPE,
            (TomlValue tv) -> tv.asPrimitive().asDouble(),
            TomlPrimitive::of
    );

    /**
     * Adapts TOML strings to/from Java {@code char}/{@link Character} fields.
     * Asserts that a TOML string is exactly 1 {@code char} (UTF-16 codepoint) in length.
     */
    TypeAdapter<Character> CHAR = of(
            Character.TYPE,
            (TomlValue tv) -> {
                TomlPrimitive tp = tv.asPrimitive();
                if (tp.isInteger()) {
                    return (char) tp.asInteger();
                } else {
                    String s = tp.asString();
                    if (s.length() != 1) throw new IllegalStateException("Cannot convert multi-char string to char");
                    return s.charAt(0);
                }
            },
            (Character c) -> TomlPrimitive.of(Character.toString(c))
    );

    /**
     * Adapts TOML strings to/from Java {@link String} fields.
     */
    TypeAdapter<String> STRING = of(
            String.class,
            (TomlValue tv) -> tv.asPrimitive().asString(),
            TomlPrimitive::of
    );

    /**
     * Adapts TOML offset date-times to/from Java {@link OffsetDateTime} fields.
     */
    TypeAdapter<OffsetDateTime> OFFSET_DATE_TIME = of(
            OffsetDateTime.class,
            (TomlValue tv) -> tv.asPrimitive().asOffsetDateTime(),
            TomlPrimitive::of
    );

    /**
     * Adapts TOML local date-times to/from Java {@link LocalDateTime} fields.
     */
    TypeAdapter<LocalDateTime> LOCAL_DATE_TIME = of(
            LocalDateTime.class,
            (TomlValue tv) -> tv.asPrimitive().asLocalDateTime(),
            TomlPrimitive::of
    );

    /**
     * Adapts TOML local dates to/from Java {@link LocalDate} fields.
     */
    TypeAdapter<LocalDate> LOCAL_DATE = of(
            LocalDate.class,
            (TomlValue tv) -> tv.asPrimitive().asLocalDate(),
            TomlPrimitive::of
    );

    /**
     * Adapts TOML local times to/from Java {@link LocalTime} fields.
     */
    TypeAdapter<LocalTime> LOCAL_TIME = of(
            LocalTime.class,
            (TomlValue tv) -> tv.asPrimitive().asLocalTime(),
            TomlPrimitive::of
    );

    //

    @ApiStatus.Internal
    @NotNull Class<T> typeClass();

    @ApiStatus.Internal
    @NotNull T toJava(@NotNull TomlValue toml);

    @ApiStatus.Internal
    @NotNull TomlValue toToml(@NotNull T java);

}
