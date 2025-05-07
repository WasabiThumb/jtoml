package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@ApiStatus.Internal
final class OffsetDateTimeTomlPrimitive extends AbstractTomlPrimitive<OffsetDateTime> {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'h:m:ss.SSSZZZZZ", Locale.ROOT);

    //

    private final OffsetDateTime value;

    public OffsetDateTimeTomlPrimitive(@NotNull OffsetDateTime value) {
        this.value = value;
    }

    //

    @Override
    public @NotNull TomlPrimitiveType type() {
        return TomlPrimitiveType.OFFSET_DATE_TIME;
    }

    @Override
    public @NotNull OffsetDateTime value() {
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
        return this.value.toInstant().toEpochMilli();
    }

    @Override
    public double asDouble() {
        return (double) this.asLong();
    }

    @Override
    public @NotNull OffsetDateTime asOffsetDateTime() {
        return this.value;
    }

    @Override
    public @NotNull LocalDateTime asLocalDateTime() {
        return this.value.toLocalDateTime();
    }

    @Override
    public @NotNull LocalDate asLocalDate() {
        return this.asLocalDateTime().toLocalDate();
    }

    @Override
    public @NotNull LocalTime asLocalTime() {
        return this.asLocalDateTime().toLocalTime();
    }

}
