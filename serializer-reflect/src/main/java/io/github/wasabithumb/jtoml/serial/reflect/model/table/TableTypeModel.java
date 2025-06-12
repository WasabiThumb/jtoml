package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.model.TypeModel;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.util.RecordSupport;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.*;

import java.util.*;

@ApiStatus.Internal
public interface TableTypeModel<T> extends TypeModel<T> {

    @SuppressWarnings("unchecked")
    static <O> @Nullable TableTypeModel<O> match(@NotNull ParameterizedClass<O> pc) {
        Class<O> raw = pc.raw();

        // Record
        if (RecordSupport.isRecord(raw))
            return new RecordTableTypeModel<>(raw);

        // TomlSerializable
        if (TomlSerializable.class.isAssignableFrom(raw))
            return (TableTypeModel<O>) SerializableTableTypeModel.create(raw);

        // TomlTable
        if (TomlTable.class.equals(raw))
            return (TableTypeModel<O>) TomlTableTypeModel.INSTANCE;

        // Map<String, ?>
        ParameterizedClass<?> mt = pc.declaredInterface(Map.class);
        if (mt != null && mt.paramCount() >= 2 && String.class.equals(mt.param(0))) {
            ParameterizedClass<?> vt = ParameterizedClass.of(mt.param(1));
            return (TableTypeModel<O>) StringMapTableTypeModel.create(raw.asSubclass(Map.class), vt);
        }

        // Other
        return null;
    }

    //

    @NotNull Builder<T> create();

    @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull T instance);

    @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key);

    @UnknownNullability Object get(@NotNull T instance, @NotNull TomlKey key);

    default void applyTableComments(@NotNull Comments comments) { }

    @Contract(mutates = "param2")
    default void applyFieldComments(@NotNull TomlKey key, @NotNull Comments comments) { }

    //

    interface Builder<O> {

        void set(@NotNull TomlKey key, @NotNull Object value);

        @NotNull O build();

    }

}
