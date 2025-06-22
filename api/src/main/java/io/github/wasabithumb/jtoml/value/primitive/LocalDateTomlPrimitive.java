package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.except.TomlValueException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.*;

@ApiStatus.Internal
final class LocalDateTomlPrimitive extends AbstractTomlPrimitive<LocalDate> {

    private final LocalDate value;
    private final ZoneOffset offset;

    public LocalDateTomlPrimitive(
            @NotNull Comments comments,
            @NotNull LocalDate value,
            @NotNull ZoneOffset offset
    ) {
        super(comments);
        TomlValueException.checkDate(value);
        this.value = value;
        this.offset = offset;
    }

    public LocalDateTomlPrimitive(
            @NotNull LocalDate value,
            @NotNull ZoneOffset offset
    ) {
        this(Comments.empty(), value, offset);
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
    @NotNull ZoneOffset temporalOffset() {
        return this.offset;
    }

    @Override
    public @NotNull String asString() {
        StringBuilder sb = new StringBuilder();
        writeDate(sb, this.value);
        return sb.toString();
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
