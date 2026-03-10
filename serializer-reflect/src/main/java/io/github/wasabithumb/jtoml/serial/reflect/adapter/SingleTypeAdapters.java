package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@ApiStatus.Internal
final class SingleTypeAdapters extends AbstractTypeAdapters {

    private final TypeAdapter<?> value;

    SingleTypeAdapters(TypeAdapter<?> value) {
        this.value = value;
    }

    //

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> TypeAdapter<T> get(@NotNull Class<T> type) {
        return this.value.typeClass().equals(type) ?
                (TypeAdapter<T>) this.value :
                null;
    }

    @Override
    protected boolean canFlatten() {
        return true;
    }

    @Override
    protected void flatten(@NotNull Consumer<? super TypeAdapter<?>> consumer) {
        consumer.accept(this.value);
    }

}
