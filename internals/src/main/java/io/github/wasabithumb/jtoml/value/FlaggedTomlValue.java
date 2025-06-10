package io.github.wasabithumb.jtoml.value;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TomlValue} with extra metadata
 * required for spec-compliant reading
 */
@ApiStatus.Internal
public final class FlaggedTomlValue implements TomlValue {

    public static @NotNull FlaggedTomlValue wrap(@NotNull TomlValue value) {
        if (value instanceof FlaggedTomlValue) return (FlaggedTomlValue) value;
        return new FlaggedTomlValue(value);
    }

    public static boolean isConstant(@NotNull TomlValue value) {
        if (value instanceof FlaggedTomlValue) return ((FlaggedTomlValue) value).isConstant();
        return false;
    }

    public static boolean isNonReusable(@NotNull TomlValue value) {
        if (value instanceof FlaggedTomlValue) return ((FlaggedTomlValue) value).isNonReusable();
        return false;
    }

    public static boolean isNonKeyExtendable(@NotNull TomlValue value) {
        if (value instanceof FlaggedTomlValue) return ((FlaggedTomlValue) value).isNonKeyExtendable();
        return false;
    }

    private static final int F_CONSTANT = 1;
    private static final int F_NON_REUSABLE = 2;
    private static final int F_NON_KEY_EXTENDABLE = 4;

    //

    private final TomlValue backing;
    private int flags = 0;

    private FlaggedTomlValue(@NotNull TomlValue backing) {
        this.backing = backing;
    }

    //

    @Contract(pure = true)
    public @NotNull TomlValue handle() {
        return this.backing;
    }

    private boolean getFlag(@MagicConstant(valuesFromClass = FlaggedTomlValue.class) int f) {
        return (this.flags & f) != 0;
    }

    private void setFlag(@MagicConstant(valuesFromClass = FlaggedTomlValue.class) int f, boolean value) {
        if (value) {
            this.flags |= f;
        } else {
            this.flags &= (~f);
        }
    }

    public boolean isConstant() {
        return this.getFlag(F_CONSTANT);
    }

    public void setConstant(boolean constant) {
        this.setFlag(F_CONSTANT, constant);
    }

    public boolean isNonReusable() {
        return this.getFlag(F_NON_REUSABLE);
    }

    public void setNonReusable(boolean nonReusable) {
        this.setFlag(F_NON_REUSABLE, nonReusable);
    }

    public boolean isNonKeyExtendable() {
        return this.getFlag(F_NON_KEY_EXTENDABLE);
    }

    public void setNonKeyExtendable(boolean nonKeyExtendable) {
        this.setFlag(F_NON_KEY_EXTENDABLE, nonKeyExtendable);
    }

    // START Super

    @Override
    public @NotNull Comments comments() {
        return this.backing.comments();
    }

    @Override
    public boolean isPrimitive() {
        return this.backing.isPrimitive();
    }

    @Override
    public @NotNull TomlPrimitive asPrimitive() throws UnsupportedOperationException {
        return this.backing.asPrimitive();
    }

    @Override
    public boolean isArray() {
        return this.backing.isArray();
    }

    @Override
    public @NotNull TomlArray asArray() throws UnsupportedOperationException {
        return this.backing.asArray();
    }

    @Override
    public boolean isTable() {
        return this.backing.isTable();
    }

    @Override
    public @NotNull TomlTable asTable() throws UnsupportedOperationException {
        return this.backing.asTable();
    }

    @Override
    public int hashCode() {
        return this.backing.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TomlValue)) return false;
        if (obj instanceof FlaggedTomlValue) {
            return this.backing.equals(((FlaggedTomlValue) obj).backing);
        } else {
            return this.backing.equals(obj);
        }
    }

    @Override
    public @NotNull String toString() {
        return this.backing.toString();
    }

    // END Super

}
