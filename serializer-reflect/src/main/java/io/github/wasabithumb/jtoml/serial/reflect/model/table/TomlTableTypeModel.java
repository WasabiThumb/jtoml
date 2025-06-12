package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

@ApiStatus.Internal
final class TomlTableTypeModel extends AbstractTableTypeModel<TomlTable> {

    static final TomlTableTypeModel INSTANCE = new TomlTableTypeModel();

    //

    @Override
    public @NotNull Class<TomlTable> type() {
        return TomlTable.class;
    }

    @Override
    public @NotNull TableTypeModel.Builder<TomlTable> create() {
        return new Builder();
    }

    @Override
    public @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull TomlTable instance) {
        return instance.keys(false);
    }

    @Override
    public @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key) {
        return new ParameterizedClass<>(TomlValue.class);
    }

    @Override
    public @UnknownNullability Object get(@NotNull TomlTable instance, @NotNull TomlKey key) {
        return instance.get(key);
    }

    //

    private static final class Builder implements TableTypeModel.Builder<TomlTable> {

        private final TomlTable table = TomlTable.create();

        @Override
        public void set(@NotNull TomlKey key, @NotNull Object value) {
            this.table.put(key, (TomlValue) value);
        }

        @Override
        public @NotNull TomlTable build() {
            return this.table;
        }

    }

}
