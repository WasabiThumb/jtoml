package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.except.TomlValueException;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.*;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a TOML primitive (not a table or array)
 * @see #value()
 */
@ApiStatus.NonExtendable
public interface TomlPrimitive extends TomlValue {

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#STRING STRING}
     * wrapping the given string value
     * @throws NullPointerException Value is null
     */
    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(String value) {
        return new StringTomlPrimitive(Objects.requireNonNull(value));
    }

    /**
     * Provides a primitive of type {@link TomlPrimitiveType#BOOLEAN BOOLEAN}
     * wrapping the given boolean value
     */
    @Contract("_ -> new")
    static @NotNull TomlPrimitive of(boolean value) {
        return new BooleanTomlPrimitive(value);
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#INTEGER INTEGER}
     * wrapping the given long value
     */
    @Contract("_ -> new")
    static @NotNull TomlPrimitive of(long value) {
        return new IntegerTomlPrimitive(value);
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#INTEGER INTEGER}
     * wrapping the given integer value after a widening conversion to {@link Long}
     */
    @Contract("_ -> new")
    static @NotNull TomlPrimitive of(int value) {
        return of((long) value);
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#FLOAT FLOAT}
     * wrapping the given double value
     */
    static @NotNull TomlPrimitive of(double value) {
        return new FloatTomlPrimitive(value);
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#FLOAT FLOAT}
     * wrapping the given float value after a widening conversion to {@link Double}
     */
    static @NotNull TomlPrimitive of(float value) {
        return of((double) value);
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#OFFSET_DATE_TIME OFFSET_DATE_TIME}
     * wrapping the given {@link OffsetDateTime}
     * @throws NullPointerException Value is null
     * @throws TomlValueException Provided date has a year outside the range 0 to 9999
     */
    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(OffsetDateTime value) throws TomlValueException {
        return new OffsetDateTimeTomlPrimitive(Objects.requireNonNull(value));
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#LOCAL_DATE_TIME LOCAL_DATE_TIME}
     * wrapping the given {@link LocalDateTime}
     * @param offset Offset to use when coercing this value to an {@link OffsetDateTime}.
     *               If null, {@link ZoneOffset#UTC} is used. This does not change the value
     *               of the resulting primitive.
     * @throws NullPointerException Value is null
     * @throws TomlValueException Provided date has a year outside the range 0 to 9999
     */
    @Contract("null, _ -> fail; !null, _ -> new")
    static @NotNull TomlPrimitive of(LocalDateTime value, @Nullable ZoneOffset offset) throws TomlValueException {
        return new LocalDateTimeTomlPrimitive(
                Objects.requireNonNull(value),
                (offset == null) ? ZoneOffset.UTC : offset
        );
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#LOCAL_DATE_TIME LOCAL_DATE_TIME}
     * wrapping the given {@link LocalDateTime}
     * @throws NullPointerException Value is null
     * @throws TomlValueException Provided date has a year outside the range 0 to 9999
     * @see #of(LocalDateTime, ZoneOffset)
     */
    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(LocalDateTime value) throws TomlValueException {
        return of(value, null);
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#LOCAL_DATE LOCAL_DATE}
     * wrapping the given {@link LocalDate}
     * @param offset Offset to use when coercing this value to an {@link OffsetDateTime}.
     *               If null, {@link ZoneOffset#UTC} is used. This does not change the value
     *               of the resulting primitive.
     * @throws NullPointerException Value is null
     * @throws TomlValueException Provided date has a year outside the range 0 to 9999
     */
    @Contract("null, _ -> fail; !null, _ -> new")
    static @NotNull TomlPrimitive of(LocalDate value, @Nullable ZoneOffset offset) throws TomlValueException {
        return new LocalDateTomlPrimitive(
                Objects.requireNonNull(value),
                (offset == null) ? ZoneOffset.UTC : offset
        );
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#LOCAL_DATE LOCAL_DATE}
     * wrapping the given {@link LocalDate}
     * @throws NullPointerException Value is null
     * @throws TomlValueException Provided date has a year outside the range 0 to 9999
     * @see #of(LocalDate, ZoneOffset)
     */
    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(LocalDate value) throws TomlValueException {
        return of(value, null);
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#LOCAL_TIME LOCAL_TIME}
     * wrapping the given {@link LocalTime}
     * @param offset Offset to use when coercing this value to an {@link OffsetDateTime}.
     *               If null, {@link ZoneOffset#UTC} is used. This does not change the value
     *               of the resulting primitive.
     * @throws NullPointerException Value is null
     * @throws TomlValueException Provided date has a year outside the range 0 to 9999
     */
    @Contract("null, _ -> fail; !null, _ -> new")
    static @NotNull TomlPrimitive of(LocalTime value, @Nullable ZoneOffset offset) {
        return new LocalTimeTomlPrimitive(
                Objects.requireNonNull(value),
                (offset == null) ? ZoneOffset.UTC : offset
        );
    }

    /**
     * Creates a primitive of type {@link TomlPrimitiveType#LOCAL_TIME LOCAL_TIME}
     * wrapping the given {@link LocalTime}
     * @throws NullPointerException Value is null
     * @throws TomlValueException Provided date has a year outside the range 0 to 9999
     * @see #of(LocalTime, ZoneOffset)
     */
    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(LocalTime value) {
        return of(value, null);
    }

    /**
     * Creates a new TomlPrimitive with the same value and comments
     * as the provided primitive
     */
    @Contract("_ -> new")
    @ApiStatus.AvailableSince("0.6.4")
    static @NotNull TomlPrimitive copyOf(@NotNull TomlPrimitive other) {
        Comments comments = Comments.copyOf(other.comments());
        switch (other.type()) {
            case BOOLEAN:
                return new BooleanTomlPrimitive(comments, other.asBoolean());
            case FLOAT:
                return new FloatTomlPrimitive(comments, other.asDouble());
            case INTEGER:
                return new IntegerTomlPrimitive(comments, other.asLong());
            case STRING:
                return new StringTomlPrimitive(comments, other.asString());
            case OFFSET_DATE_TIME:
                return new OffsetDateTimeTomlPrimitive(comments, other.asOffsetDateTime());
            case LOCAL_DATE_TIME:
                return new LocalDateTimeTomlPrimitive(
                        comments,
                        other.asLocalDateTime(),
                        ((AbstractTomlPrimitive<?>) other).temporalOffset()
                );
            case LOCAL_DATE:
                return new LocalDateTomlPrimitive(
                        comments,
                        other.asLocalDate(),
                        ((AbstractTomlPrimitive<?>) other).temporalOffset()
                );
            case LOCAL_TIME:
                return new LocalTimeTomlPrimitive(
                        comments,
                        other.asLocalTime(),
                        ((AbstractTomlPrimitive<?>) other).temporalOffset()
                );
            default:
                throw new AssertionError("Unreachable code");
        }
    }

    /**
     * Parses a float string into a
     * {@link TomlPrimitive} with type {@link TomlPrimitiveType#FLOAT FLOAT}.
     * @throws NullPointerException Provided string is null
     * @throws IllegalArgumentException Provided string is not a valid float
     */
    @Contract("null -> fail; _ -> new")
    static @NotNull TomlPrimitive parseFloat(String string) throws IllegalArgumentException {
        if (string == null) throw new NullPointerException("Cannot parse null as float");
        return FloatTomlPrimitive.parse(string);
    }

    //

    /**
     * The actual type of this primitive
     * @see #value()
     */
    @NotNull TomlPrimitiveType type();

    /**
     * The (boxed) value of this primitive. Will be one of
     * {@link String}, {@link Boolean}, {@link Long}, {@link Double},
     * {@link OffsetDateTime}, {@link LocalDateTime}, {@link LocalDate} or
     * {@link LocalTime}.
     * @see #type()
     */
    @NotNull Serializable value();

    //

    /**
     * Checks if the {@link #type() type} of this primitive is
     * equal to {@link TomlPrimitiveType#STRING}, meaning that
     * the output of {@link #asString()} is identical to the
     * {@link #value() value} of this primitive.
     */
    default boolean isString() {
        return this.type() == TomlPrimitiveType.STRING;
    }

    /**
     * Coerces the {@link #value() value} of this
     * primitive to a string.
     */
    @NotNull String asString();

    //

    /**
     * Checks if the {@link #type() type} of this primitive is
     * equal to {@link TomlPrimitiveType#BOOLEAN}, meaning that
     * the output of {@link #asBoolean()} is equal to the
     * {@link #value() value} of this primitive.
     */
    default boolean isBoolean() {
        return this.type() == TomlPrimitiveType.BOOLEAN;
    }

    /**
     * <p>
     *     Coerces the {@link #value() value} of this
     *     primitive to a boolean.
     * </p>
     * <ul>
     *     <li>
     *         Boolean primitives will return their
     *         stored value
     *     </li>
     *     <li>
     *         String primitives equal to {@code "false"} will
     *         return false, otherwise true
     *     </li>
     *     <li>
     *         Date-Time primitives will return true
     *     </li>
     *     <li>
     *         Integer and Float primitives that are arithmetically
     *         equal to 0 will return false, otherwise true
     *     </li>
     * </ul>
     */
    boolean asBoolean();

    //

    /**
     * Checks if the {@link #type() type} of this primitive is
     * equal to {@link TomlPrimitiveType#INTEGER}, meaning that
     * the output of {@link #asLong()} is equal to the
     * {@link #value() value} of this primitive.
     */
    default boolean isInteger() {
        return this.type() == TomlPrimitiveType.INTEGER;
    }

    /**
     * Coerces the {@link #value() value} of this primitive to an integer.
     * <ul>
     *     <li>
     *         Integer primitives will return their stored value
     *     </li>
     *     <li>
     *         String primitives will attempt to parse their value as a
     *         long, throwing {@link NumberFormatException} if not possible
     *     </li>
     *     <li>
     *         Date-Time primitives will return their value after coercion to
     *         {@link OffsetDateTime} as epoch millis
     *     </li>
     *     <li>
     *         Boolean primitives will return 1 if true and 0 if false
     *     </li>
     * </ul>
     * @throws ArithmeticException The intrinsic or coerced value of this primitive
     *                             (as a long) cannot be losslessly converted to an integer
     * @see #asLong()
     */
    default int asInteger() throws ArithmeticException {
        return Math.toIntExact(this.asLong());
    }

    /**
     * Coerces the {@link #value() value} of this primitive to a long.
     * <ul>
     *     <li>
     *         Integer primitives will return their stored value
     *     </li>
     *     <li>
     *         String primitives will attempt to parse their value as a
     *         long, throwing {@link NumberFormatException} if not possible
     *     </li>
     *     <li>
     *         Date-Time primitives will return their value after coercion to
     *         {@link OffsetDateTime} as epoch millis
     *     </li>
     *     <li>
     *         Boolean primitives will return 1 if true and 0 if false
     *     </li>
     * </ul>
     * @see #asInteger()
     */
    long asLong();

    //

    /**
     * Checks if the {@link #type() type} of this primitive is
     * equal to {@link TomlPrimitiveType#FLOAT}, meaning that
     * the output of {@link #asDouble()} is equal to the
     * {@link #value() value} of this primitive.
     */
    default boolean isFloat() {
        return this.type() == TomlPrimitiveType.FLOAT;
    }

    /**
     * Coerces the {@link #value() value} of this primitive to a float.
     * Output is identical to that of {@link #asDouble()} after narrowing conversion
     * to {@code float}.
     * @see #asDouble()
     */
    default float asFloat() {
        return (float) this.asDouble();
    }

    /**
     * Coerces the {@link #value() value} of this primitive to a double.
     * If this is a float primitive, returns the stored value. Otherwise, returns
     * the closest double value to that of {@link #asLong()}.
     * @see #asFloat()
     */
    double asDouble();

    //

    /**
     * Checks if the {@link #type() type} of this primitive is
     * equal to {@link TomlPrimitiveType#OFFSET_DATE_TIME}, meaning that
     * the output of {@link #asOffsetDateTime()} is identical to the
     * {@link #value() value} of this primitive.
     */
    default boolean isOffsetDateTime() {
        return this.type() == TomlPrimitiveType.OFFSET_DATE_TIME;
    }

    /**
     * Returns a {@link Instant} that represents the same moment in time
     * as the output of {@link #asOffsetDateTime()}.
     */
    default @NotNull Instant asInstant() {
        return this.asOffsetDateTime().toInstant();
    }

    /**
     * Returns a {@link Date} that represents the same moment in time
     * as the output of {@link #asOffsetDateTime()}.
     */
    default @NotNull Date asDate() {
        return Date.from(this.asInstant());
    }

    /**
     * Coerces the {@link #value() value} of this primitive to an {@link OffsetDateTime}.
     * <ul>
     *     <li>For Offset Date-Time primitives, returns the stored value</li>
     *     <li>For Local Date-Time primitives, pairs the stored value with the configured offset</li>
     *     <li>For Local Date primitives, evaluates the stored date at the 0th hour and configured offset</li>
     *     <li>For Local Time primitives, evaluates the stored time at the 0th date and configured offset</li>
     *     <li>For Integer primitives, interprets the stored value as epoch millis</li>
     *     <li>For all other values, throws {@link UnsupportedOperationException}</li>
     * </ul>
     */
    default @NotNull OffsetDateTime asOffsetDateTime() {
        throw new UnsupportedOperationException();
    }

    //

    /**
     * Checks if the {@link #type() type} of this primitive is
     * equal to {@link TomlPrimitiveType#LOCAL_DATE_TIME}, meaning that
     * the output of {@link #asLocalDateTime()} is identical to the
     * {@link #value() value} of this primitive.
     */
    default boolean isLocalDateTime() {
        return this.type() == TomlPrimitiveType.LOCAL_DATE_TIME;
    }

    /**
     * Coerces the {@link #value() value} of this primitive to a {@link LocalDateTime}.
     * <ul>
     *     <li>For Offset Date-Time primitives, returns the stored value after discarding the offset</li>
     *     <li>For Local Date-Time primitives, returns the stored value</li>
     *     <li>For Local Date primitives, evaluates the stored date at the 0th hour</li>
     *     <li>For Local Time primitives, evaluates the stored time at the 0th date</li>
     *     <li>For Integer primitives, interprets the stored value as epoch millis</li>
     *     <li>For all other values, throws {@link UnsupportedOperationException}</li>
     * </ul>
     */
    default @NotNull LocalDateTime asLocalDateTime() {
        throw new UnsupportedOperationException();
    }

    //

    /**
     * Checks if the {@link #type() type} of this primitive is
     * equal to {@link TomlPrimitiveType#LOCAL_DATE}, meaning that
     * the output of {@link #asLocalDate()} is identical to the
     * {@link #value() value} of this primitive.
     */
    default boolean isLocalDate() {
        return this.type() == TomlPrimitiveType.LOCAL_DATE;
    }

    /**
     * Coerces the {@link #value() value} of this primitive to a {@link LocalDate}.
     * <ul>
     *     <li>For Offset Date-Time primitives, returns the stored value after discarding the time and offset</li>
     *     <li>For Local Date-Time primitives, returns the stored value after discarding the time</li>
     *     <li>For Local Date primitives, returns the stored date</li>
     *     <li>For Local Time primitives, returns the 0th date</li>
     *     <li>For all other values, throws {@link UnsupportedOperationException}</li>
     * </ul>
     */
    default @NotNull LocalDate asLocalDate() {
        throw new UnsupportedOperationException();
    }

    //

    /**
     * Checks if the {@link #type() type} of this primitive is
     * equal to {@link TomlPrimitiveType#LOCAL_TIME}, meaning that
     * the output of {@link #asLocalTime()} is identical to the
     * {@link #value() value} of this primitive.
     */
    default boolean isLocalTime() {
        return this.type() == TomlPrimitiveType.LOCAL_TIME;
    }

    /**
     * Coerces the {@link #value() value} of this primitive to a {@link LocalTime}.
     * <ul>
     *     <li>For Offset Date-Time primitives, returns the stored value after discarding the date and offset</li>
     *     <li>For Local Date-Time primitives, returns the stored value after discarding the date</li>
     *     <li>For Local Date primitives, returns the 0th hour</li>
     *     <li>For Local Time primitives, returns the stored value</li>
     *     <li>For all other values, throws {@link UnsupportedOperationException}</li>
     * </ul>
     */
    default @NotNull LocalTime asLocalTime() {
        throw new UnsupportedOperationException();
    }

}
