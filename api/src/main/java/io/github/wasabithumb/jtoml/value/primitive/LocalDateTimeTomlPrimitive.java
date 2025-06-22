package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.except.TomlValueException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.*;

@ApiStatus.Internal
final class LocalDateTimeTomlPrimitive extends AbstractTomlPrimitive<LocalDateTime> {

    private final LocalDateTime value;
    private final ZoneOffset offset;

    public LocalDateTimeTomlPrimitive(
            @NotNull Comments comments,
            @NotNull LocalDateTime value,
            @NotNull ZoneOffset offset
    ) {
        super(comments);
        TomlValueException.checkDate(value);
        this.value = value;
        this.offset = offset;
    }

    public LocalDateTimeTomlPrimitive(
            @NotNull LocalDateTime value,
            @NotNull ZoneOffset offset
    ) {
        this(Comments.empty(), value, offset);
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
    @NotNull ZoneOffset temporalOffset() {
        return this.offset;
    }

    @Override
    public @NotNull String asString() {
        StringBuilder sb = new StringBuilder();
        writeDate(sb, this.value.toLocalDate());
        sb.append('T');
        writeTime(sb, this.value.toLocalTime());
        return sb.toString();
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
