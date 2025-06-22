package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class BooleanTomlPrimitive extends AbstractTomlPrimitive<Boolean> {

    private final boolean value;

    public BooleanTomlPrimitive(@NotNull Comments comments, boolean value) {
        super(comments);
        this.value = value;
    }

    public BooleanTomlPrimitive(boolean value) {
        this(Comments.empty(), value);
    }

    //

    @Override
    public @NotNull TomlPrimitiveType type() {
        return TomlPrimitiveType.BOOLEAN;
    }

    @Override
    public @NotNull Boolean value() {
        return this.value;
    }

    @Override
    public @NotNull String asString() {
        return this.value ? "true" : "false";
    }

    @Override
    public boolean asBoolean() {
        return this.value;
    }

    @Override
    public long asLong() {
        return this.value ? 1L : 0L;
    }

    @Override
    public double asDouble() {
        return this.value ? 1d : 0d;
    }

}
