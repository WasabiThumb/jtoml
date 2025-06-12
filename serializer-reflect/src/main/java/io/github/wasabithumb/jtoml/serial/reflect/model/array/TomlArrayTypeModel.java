package io.github.wasabithumb.jtoml.serial.reflect.model.array;

import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

@ApiStatus.Internal
final class TomlArrayTypeModel implements ArrayTypeModel<TomlArray> {

    static final TomlArrayTypeModel INSTANCE = new TomlArrayTypeModel();

    //

    @Override
    public @NotNull Class<TomlArray> type() {
        return TomlArray.class;
    }

    @Override
    public @NotNull ParameterizedClass<?> componentType() {
        return new ParameterizedClass<>(TomlValue.class);
    }

    @Override
    public @NotNull TomlArray createNew(int length) {
        return TomlArray.create(length);
    }

    @Override
    public int size(@NotNull TomlArray instance) {
        return instance.size();
    }

    @Override
    public @UnknownNullability Object get(@NotNull TomlArray instance, int index) {
        return instance.get(index);
    }

    @Override
    public void set(@NotNull TomlArray instance, int index, @NotNull Object object) {
        if (index == instance.size()) {
            instance.add((TomlValue) object);
        } else {
            instance.set(index, (TomlValue) object);
        }
    }

}
