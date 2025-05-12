package io.github.wasabithumb.jtoml.value.primitive;

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

    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(String value) {
        return new StringTomlPrimitive(Objects.requireNonNull(value));
    }

    static @NotNull TomlPrimitive of(boolean value) {
        return value ? BooleanTomlPrimitive.TRUE : BooleanTomlPrimitive.FALSE;
    }

    static @NotNull TomlPrimitive of(long value) {
        return new IntegerTomlPrimitive(value);
    }

    static @NotNull TomlPrimitive of(int value) {
        return of((long) value);
    }

    static @NotNull TomlPrimitive of(double value) {
        return new FloatTomlPrimitive(value);
    }

    static @NotNull TomlPrimitive of(float value) {
        return of((double) value);
    }

    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(OffsetDateTime value) throws TomlValueException {
        return new OffsetDateTimeTomlPrimitive(Objects.requireNonNull(value));
    }

    @Contract("null, _ -> fail; !null, _ -> new")
    static @NotNull TomlPrimitive of(LocalDateTime value, @Nullable ZoneOffset offset) throws TomlValueException {
        return new LocalDateTimeTomlPrimitive(
                Objects.requireNonNull(value),
                (offset == null) ? ZoneOffset.UTC : offset
        );
    }

    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(LocalDateTime value) throws TomlValueException {
        return of(value, null);
    }

    @Contract("null, _ -> fail; !null, _ -> new")
    static @NotNull TomlPrimitive of(LocalDate value, @Nullable ZoneOffset offset) throws TomlValueException {
        return new LocalDateTomlPrimitive(
                Objects.requireNonNull(value),
                (offset == null) ? ZoneOffset.UTC : offset
        );
    }

    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(LocalDate value) throws TomlValueException {
        return of(value, null);
    }

    @Contract("null, _ -> fail; !null, _ -> new")
    static @NotNull TomlPrimitive of(LocalTime value, @Nullable ZoneOffset offset) {
        return new LocalTimeTomlPrimitive(
                Objects.requireNonNull(value),
                (offset == null) ? ZoneOffset.UTC : offset
        );
    }

    @Contract("null -> fail; !null -> new")
    static @NotNull TomlPrimitive of(LocalTime value) {
        return of(value, null);
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

    default boolean isString() {
        return this.type() == TomlPrimitiveType.STRING;
    }

    @NotNull String asString();

    //

    default boolean isBoolean() {
        return this.type() == TomlPrimitiveType.BOOLEAN;
    }

    boolean asBoolean();

    //

    default boolean isInteger() {
        return this.type() == TomlPrimitiveType.INTEGER;
    }

    default int asInteger() throws ArithmeticException {
        return Math.toIntExact(this.asLong());
    }

    long asLong();

    //

    default boolean isFloat() {
        return this.type() == TomlPrimitiveType.FLOAT;
    }

    default float asFloat() {
        return (float) this.asDouble();
    }

    double asDouble();

    //

    default boolean isOffsetDateTime() {
        return this.type() == TomlPrimitiveType.OFFSET_DATE_TIME;
    }

    default @NotNull Instant asInstant() {
        return this.asOffsetDateTime().toInstant();
    }

    default @NotNull Date asDate() {
        return Date.from(this.asInstant());
    }

    default @NotNull OffsetDateTime asOffsetDateTime() {
        throw new UnsupportedOperationException();
    }

    //

    default boolean isLocalDateTime() {
        return this.type() == TomlPrimitiveType.LOCAL_DATE_TIME;
    }

    default @NotNull LocalDateTime asLocalDateTime() {
        throw new UnsupportedOperationException();
    }

    //

    default boolean isLocalDate() {
        return this.type() == TomlPrimitiveType.LOCAL_DATE;
    }

    default @NotNull LocalDate asLocalDate() {
        throw new UnsupportedOperationException();
    }

    //

    default boolean isLocalTime() {
        return this.type() == TomlPrimitiveType.LOCAL_TIME;
    }

    default @NotNull LocalTime asLocalTime() {
        throw new UnsupportedOperationException();
    }

}
