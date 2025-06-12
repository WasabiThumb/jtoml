package io.github.wasabithumb.jtoml.serial.reflect.model.array;

import io.github.wasabithumb.jtoml.serial.reflect.model.TypeModel;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ApiStatus.Internal
public interface ArrayTypeModel<T> extends TypeModel<T> {

    @SuppressWarnings("unchecked")
    static <O> @Nullable ArrayTypeModel<O> match(@NotNull ParameterizedClass<O> pc) {
        Class<O> raw = pc.raw();

        // TomlArray
        if (TomlArray.class.equals(raw))
            return (ArrayTypeModel<O>) TomlArrayTypeModel.INSTANCE;

        // Array
        if (raw.isArray())
            return new DirectArrayTypeModel<>(raw, raw.getComponentType());

        // List<?>
        ParameterizedClass<?> list = pc.declaredInterface(List.class);
        if (list != null && list.paramCount() >= 1) {
            ParameterizedClass<?> elementType = ParameterizedClass.of(list.param(0));
            return (ArrayTypeModel<O>) ListArrayTypeModel.create(raw.asSubclass(List.class), elementType);
        }

        // Other
        return null;
    }

    //

    @NotNull ParameterizedClass<?> componentType();

    @NotNull T createNew(int length);

    int size(@NotNull T instance);

    @UnknownNullability Object get(@NotNull T instance, int index);

    void set(@NotNull T instance, int index, @NotNull Object object);

}
