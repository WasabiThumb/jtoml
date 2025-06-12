package io.github.wasabithumb.jtoml.serial.reflect;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.serial.reflect.adapter.TypeAdapter;
import io.github.wasabithumb.jtoml.serial.reflect.adapter.TypeAdapters;
import io.github.wasabithumb.jtoml.serial.reflect.model.array.ArrayTypeModel;
import io.github.wasabithumb.jtoml.serial.reflect.model.table.TableTypeModel;
import io.github.wasabithumb.jtoml.serial.reflect.model.TypeModel;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.util.ReferenceHolder;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.NotNull;

final class ReflectTomlSerializer<T> implements TomlSerializer.Symmetric<T> {

    private final TableTypeModel<T> model;

    ReflectTomlSerializer(@NotNull Class<T> clazz) {
        this.model = TypeModel.of(new ParameterizedClass<>(clazz))
                .asTable();
    }

    //

    @Override
    public @NotNull Class<T> serialType() {
        return this.model.type();
    }

    @Override
    public @NotNull T serialize(@NotNull TomlTable table) {
        return this.serializeTable(
                this.model,
                table
        );
    }

    @Override
    public @NotNull TomlTable deserialize(@NotNull T data) {
        ReferenceHolder parents = new ReferenceHolder();
        parents.add(this);
        return this.deserializeTable(
                parents,
                this.model,
                data
        );
    }

    //

    private <E> @NotNull E serializeValue(
            @NotNull TypeModel<E> model,
            @NotNull TomlValue value
    ) {
        if (model.isArray()) return this.serializeArray(model.asArray(), value.asArray());
        if (model.isTable()) return this.serializeTable(model.asTable(), value.asTable());

        TypeAdapter<E> adapter = TypeAdapters.get(model.type());
        return adapter.toJava(value.asPrimitive());
    }

    private <E> @NotNull E serializeArray(
            @NotNull ArrayTypeModel<E> model,
            @NotNull TomlArray array
    ) {
        final int len = array.size();
        E ret = model.createNew(len);
        TypeModel<?> elementModel = TypeModel.of(model.componentType());

        for (int i=0; i < len; i++) {
            TomlValue value = array.get(i);
            Object object = this.serializeValue(elementModel, value);
            model.set(ret, i, object);
        }

        return ret;
    }

    private <E> @NotNull E serializeTable(
            @NotNull TableTypeModel<E> model,
            @NotNull TomlTable table
    ) {
        TableTypeModel.Builder<E> builder = model.create();

        for (TomlKey tk : table.keys(false)) {
            TomlValue value = table.get(tk);
            assert value != null;

            TypeModel<?> valueModel = TypeModel.of(model.elementType(tk));
            Object object = this.serializeValue(valueModel, value);
            builder.set(tk, object);
        }

        return builder.build();
    }

    //

    @SuppressWarnings("unchecked")
    private <E> @NotNull TomlValue deserializeValueUnsafe(
            @NotNull ReferenceHolder parents,
            @NotNull TypeModel<E> model,
            @NotNull Object value
    ) {
        return this.deserializeValue(parents, model, (E) value);
    }

    private <E> @NotNull TomlValue deserializeValue(
            @NotNull ReferenceHolder parents,
            @NotNull TypeModel<E> model,
            @NotNull E value
    ) {
        if (!parents.add(value))
            throw new IllegalArgumentException("Cannot deserialize recursive data (" + value + " refers to itself)");

        if (model.isArray()) return this.deserializeArray(parents, model.asArray(), value);
        if (model.isTable()) return this.deserializeTable(parents, model.asTable(), value);

        TypeAdapter<E> adapter = TypeAdapters.get(model.type());
        return adapter.toToml(value);
    }

    private <E> @NotNull TomlArray deserializeArray(
            @NotNull ReferenceHolder parents,
            @NotNull ArrayTypeModel<E> model,
            @NotNull E value
    ) {
        final int size = model.size(value);
        TomlArray ret = TomlArray.create(size);
        TypeModel<?> componentModel = TypeModel.of(model.componentType());

        Object next;
        TomlValue nextValue;
        for (int i=0; i < size; i++) {
            next = model.get(value, i);
            nextValue = this.deserializeValueUnsafe(
                    ReferenceHolder.copyOf(parents),
                    componentModel,
                    next
            );
            ret.add(nextValue);
        }

        return ret;
    }

    private <E> @NotNull TomlTable deserializeTable(
            @NotNull ReferenceHolder parents,
            @NotNull TableTypeModel<E> model,
            @NotNull E value
    ) {
        TomlTable ret = TomlTable.create();

        Object next;
        TomlValue nextValue;
        for (TomlKey key : model.keys(value)) {
            TypeModel<?> valueModel = TypeModel.of(model.elementType(key));
            next = model.get(value, key);
            nextValue = this.deserializeValueUnsafe(
                    ReferenceHolder.copyOf(parents),
                    valueModel,
                    next
            );
            model.applyFieldComments(key, nextValue.comments());
            ret.put(key, nextValue);
        }

        model.applyTableComments(ret.comments());
        return ret;
    }

}
