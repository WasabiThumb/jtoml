package io.github.wasabithumb.jtoml.serial.reflect.model;

import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Describes the component types of a Java type for the
 * purposes of TOML de/serialization. The components
 * are to be checked for recursion and a branch is to
 * be executed to process it.
 */
@ApiStatus.Internal
public interface TypeModel<T> {

    static <O> @NotNull TypeModel<O> of(@NotNull ParameterizedClass<O> cls) {
        TypeModel<O> candidate;

        // Tables
        candidate = TableTypeModel.match(cls);
        if (candidate != null) return candidate;

        // Arrays
        candidate = ArrayTypeModel.match(cls);
        if (candidate != null) return candidate;

        // Everything else
        return new Basic<>(cls.raw());
    }

    //

    @NotNull Class<T> type();

    default boolean isArray() {
        return this instanceof ArrayTypeModel<?>;
    }

    @Contract("-> this")
    default @NotNull ArrayTypeModel<T> asArray() throws ClassCastException {
        return (ArrayTypeModel<T>) this;
    }

    default boolean isTable() {
        return this instanceof TableTypeModel<?>;
    }

    @Contract("-> this")
    default @NotNull TableTypeModel<T> asTable() throws ClassCastException {
        return (TableTypeModel<T>) this;
    }

    //

    final class Basic<T> implements TypeModel<T> {

        private final Class<T> type;

        private Basic(@NotNull Class<T> type) {
            this.type = type;
        }

        @Override
        public @NotNull Class<T> type() {
            return this.type;
        }

    }

}
