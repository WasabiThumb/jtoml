package io.github.wasabithumb.jtoml.serial.reflect;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.serial.TomlSerializerService;
import io.github.wasabithumb.recsup.RecordSupport;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

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
        if (!RecordSupport.isRecord(outType)) {
            int mod = outType.getModifiers();
            if (!TomlSerializable.class.isAssignableFrom(outType)) {
                throw new IllegalArgumentException("Cannot create serializer for " + outType.getName() +
                        " (does not implement TomlSerializable)");
            }
            if (Modifier.isInterface(mod) || Modifier.isAbstract(mod)) {
                throw new IllegalArgumentException("Cannot create serializer for " + outType.getName() +
                        " (not directly instantiable)");
            }
        }
        return new ReflectTomlSerializer<>(outType);
    }

    @Override
    public @NotNull <T> TomlSerializer<T, ?> getDeserializer(@NotNull JToml instance, @NotNull Class<T> inType) {
        if (!RecordSupport.isRecord(inType) && !TomlSerializable.class.isAssignableFrom(inType)) {
            throw new IllegalArgumentException("Cannot create deserializer for " + inType.getName() +
                    " (does not implement TomlSerializable)");
        }
        return new ReflectTomlSerializer<>(inType);
    }

}
