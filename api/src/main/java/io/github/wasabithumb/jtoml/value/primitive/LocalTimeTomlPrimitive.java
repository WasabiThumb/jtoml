package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@ApiStatus.Internal
final class LocalTimeTomlPrimitive extends AbstractTomlPrimitive<LocalTime> {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("h:m:ss.SSS", Locale.ROOT);

    //

    private final LocalTime value;
    private final ZoneOffset offset;

    public LocalTimeTomlPrimitive(@NotNull LocalTime value, @NotNull ZoneOffset offset) {
        this.value = value;
        this.offset = offset;
    }

    //

    @Override
    public @NotNull TomlPrimitiveType type() {
        return TomlPrimitiveType.LOCAL_TIME;
    }

    @Override
    public @NotNull LocalTime value() {
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
        return this.value.atDate(LocalDate.now()).toInstant(ZoneOffset.UTC).toEpochMilli();
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
        return this.value.atDate(LocalDate.ofEpochDay(0));
    }

    @Override
    public @NotNull LocalDate asLocalDate() {
        return LocalDate.ofEpochDay(0);
    }

    @Override
    public @NotNull LocalTime asLocalTime() {
        return this.value;
    }

}
