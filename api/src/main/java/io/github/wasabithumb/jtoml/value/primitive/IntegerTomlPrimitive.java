package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApiStatus.Internal
final class IntegerTomlPrimitive extends AbstractTomlPrimitive<Long> {

    private final long value;

    public IntegerTomlPrimitive(long value) {
        this.value = value;
    }

    //

    @Override
    public @NotNull TomlPrimitiveType type() {
        return TomlPrimitiveType.INTEGER;
    }

    @Override
    public @NotNull Long value() {
        return this.value;
    }

    @Override
    public @NotNull String asString() {
        return Long.toString(this.value);
    }

    @Override
    public boolean asBoolean() {
        return this.value != 0L;
    }

    @Override
    public long asLong() {
        return this.value;
    }

    @Override
    public double asDouble() {
        return (double) this.value;
    }

    @Override
    public @NotNull OffsetDateTime asOffsetDateTime() {
        return Instant.ofEpochMilli(this.value).atOffset(ZoneOffset.UTC);
    }

    @Override
    public @NotNull LocalDateTime asLocalDateTime() {
        return LocalDateTime.ofEpochSecond(
                this.value / 1000L,
                ((int) (this.value % 1000)) * 1000000,
                ZoneOffset.UTC
        );
    }

    @Override
    public @NotNull Instant asInstant() {
        return Instant.ofEpochMilli(this.value);
    }

}
