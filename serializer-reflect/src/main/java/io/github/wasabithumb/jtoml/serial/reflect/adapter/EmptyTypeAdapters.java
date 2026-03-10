package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@ApiStatus.Internal
final class EmptyTypeAdapters extends AbstractTypeAdapters {

    static final EmptyTypeAdapters INSTANCE = new EmptyTypeAdapters();

    //

    private EmptyTypeAdapters() { }

    //

    @Override
    public @Nullable <T> TypeAdapter<T> get(@NotNull Class<T> type) {
        return null;
    }

    @Override
    protected boolean canFlatten() {
        return true;
    }

    @Override
    protected void flatten(@NotNull Consumer<? super TypeAdapter<?>> consumer) { }

}
