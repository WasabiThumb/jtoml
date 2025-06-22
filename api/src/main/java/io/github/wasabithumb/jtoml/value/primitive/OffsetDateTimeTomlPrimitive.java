package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.except.TomlValueException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.*;

@ApiStatus.Internal
final class OffsetDateTimeTomlPrimitive extends AbstractTomlPrimitive<OffsetDateTime> {

    private final OffsetDateTime value;

    public OffsetDateTimeTomlPrimitive(
            @NotNull Comments comments,
            @NotNull OffsetDateTime value
    ) {
        super(comments);
        TomlValueException.checkDate(value);
        this.value = value;
    }

    public OffsetDateTimeTomlPrimitive(
            @NotNull OffsetDateTime value
    ) {
        this(Comments.empty(), value);
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
        StringBuilder sb = new StringBuilder();

        writeDate(sb, this.value.toLocalDate());
        sb.append('T');
        writeTime(sb, this.value.toLocalTime());

        int sec = this.value.getOffset().getTotalSeconds();
        if (sec == 0) {
            sb.append('Z');
            return sb.toString();
        } else if (sec < 0) {
            sec = -sec;
            sb.append('-');
        } else {
            sb.append('+');
        }

        int hour = sec / 3600;
        int minute = (sec % 3600) / 60;

        writeHourMinute(sb, hour, minute);
        return sb.toString();
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
