package io.github.wasabithumb.jtoml.serial.reflect;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.serial.TomlSerializerService;
import io.github.wasabithumb.jtoml.serial.reflect.adapter.TypeAdapters;
import io.github.wasabithumb.recsup.RecordSupport;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

@ApiStatus.Internal
public final class ReflectTomlSerializerService extends TomlSerializerService {

    @Override
    public boolean canSerializeTo(@NotNull Class<?> outType) {
        if (RecordSupport.isRecord(outType)) return true;
        int mod = outType.getModifiers();
        return TomlSerializable.class.isAssignableFrom(outType) &&
                !Modifier.isInterface(mod) &&
                !Modifier.isAbstract(mod);
    }

    @Override
    public boolean canDeserializeFrom(@NotNull Class<?> inType) {
        if (RecordSupport.isRecord(inType)) return true;
        return TomlSerializable.class.isAssignableFrom(inType);
    }

    //

    @Override
    public @NotNull <T> TomlSerializer<?, T> getSerializer(@NotNull JToml instance, @NotNull Class<T> outType) {
        return new ReflectTomlSerializer<>(
                outType,
                TypeAdapters.standard(),
                ReflectTomlSerializer.C_SERIALIZE
        );
    }

    @Override
    public @NotNull <T> TomlSerializer<T, ?> getDeserializer(@NotNull JToml instance, @NotNull Class<T> inType) {
        return new ReflectTomlSerializer<>(
                inType,
                TypeAdapters.standard(),
                ReflectTomlSerializer.C_DESERIALIZE
        );
    }

}
