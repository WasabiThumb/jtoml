package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@ApiStatus.Internal
final class LocalDateTimeTomlPrimitive extends AbstractTomlPrimitive<LocalDateTime> {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'h:m:ss.SSS", Locale.ROOT);

    //

    private final LocalDateTime value;
    private final ZoneOffset offset;

    public LocalDateTimeTomlPrimitive(@NotNull LocalDateTime value, @NotNull ZoneOffset offset) {
        this.value = value;
        this.offset = offset;
    }

    //

    @Override
    public @NotNull TomlPrimitiveType type() {
        return TomlPrimitiveType.LOCAL_DATE_TIME;
    }

    @Override
    public @NotNull LocalDateTime value() {
        return this.value;
    }

    @Override
    public @NotNull String asString() {
        return FORMAT.format(this.value);
    }

    @Override
    public boolean asBoolean() {
        return true;
    }

    @Override
    public long asLong() {
        return this.value.toInstant(this.offset).toEpochMilli();
    }

    @Override
    public double asDouble() {
        return (double) this.asLong();
    }

    @Override
    public @NotNull OffsetDateTime asOffsetDateTime() {
        return this.value.atOffset(this.offset);
    }

    @Override
    public @NotNull LocalDateTime asLocalDateTime() {
        return this.value;
    }

    @Override
    public @NotNull LocalDate asLocalDate() {
        return this.value.toLocalDate();
    }

    @Override
    public @NotNull LocalTime asLocalTime() {
        return this.value.toLocalTime();
    }

}
