package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@ApiStatus.Internal
final class LocalDateTomlPrimitive extends AbstractTomlPrimitive<LocalDate> {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);

    //

    private final LocalDate value;
    private final ZoneOffset offset;

    public LocalDateTomlPrimitive(@NotNull LocalDate value, @NotNull ZoneOffset offset) {
        this.value = value;
        this.offset = offset;
    }

    //

    @Override
    public @NotNull TomlPrimitiveType type() {
        return TomlPrimitiveType.LOCAL_DATE;
    }

    @Override
    public @NotNull LocalDate value() {
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
        return this.value.atTime(0, 0).toInstant(this.offset).toEpochMilli();
    }

    @Override
    public double asDouble() {
        return (double) this.asLong();
    }

    @Override
    public @NotNull OffsetDateTime asOffsetDateTime() {
        return this.asLocalDateTime().atOffset(this.offset);
    }

    @Override
    public @NotNull LocalDateTime asLocalDateTime() {
        return this.value.atTime(0, 0);
    }

    @Override
    public @NotNull LocalDate asLocalDate() {
        return this.value;
    }

    @Override
    public @NotNull LocalTime asLocalTime() {
        return LocalTime.of(0, 0);
    }

}
