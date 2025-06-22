package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.*;

@ApiStatus.Internal
final class LocalTimeTomlPrimitive extends AbstractTomlPrimitive<LocalTime> {

    private final LocalTime value;
    private final ZoneOffset offset;

    public LocalTimeTomlPrimitive(
            @NotNull Comments comments,
            @NotNull LocalTime value,
            @NotNull ZoneOffset offset
    ) {
        super(comments);
        this.value = value;
        this.offset = offset;
    }

    public LocalTimeTomlPrimitive(
            @NotNull LocalTime value,
            @NotNull ZoneOffset offset
    ) {
        this(Comments.empty(), value, offset);
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
    @NotNull ZoneOffset temporalOffset() {
        return this.offset;
    }

    @Override
    public @NotNull String asString() {
        StringBuilder sb = new StringBuilder();
        writeTime(sb, this.value);
        return sb.toString();
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
