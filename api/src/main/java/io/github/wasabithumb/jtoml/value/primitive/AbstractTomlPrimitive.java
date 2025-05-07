package io.github.wasabithumb.jtoml.value.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@ApiStatus.Internal
abstract class AbstractTomlPrimitive<T extends Serializable> implements TomlPrimitive {

    @Override
    public abstract @NotNull T value();

    @Override
    public int hashCode() {
        int h = 7;
        h = 31 * h + this.type().hashCode();
        h = 31 * h + this.value().hashCode();
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TomlPrimitive)) return false;
        TomlPrimitive other = (TomlPrimitive) obj;
        if (this.type() != other.type()) return false;
        return this.value().equals(other.value());
    }

    @Override
    public @NotNull String toString() {
        return "TomlPrimitive[type=" + this.type().name() + ", value=" + this.value() + "]";
    }

}
