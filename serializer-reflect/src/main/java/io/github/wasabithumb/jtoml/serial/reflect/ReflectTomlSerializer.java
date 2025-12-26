package io.github.wasabithumb.jtoml.serial.reflect;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
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
import io.github.wasabithumb.recsup.RecordSupport;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

/**
 * Handles reflection-powered conversion of TOML tables
 * to/from a given table-like Java type. Supports
 * both records and POJOs marked with {@link TomlSerializable}.
 * @see #fromToml(TomlTable)
 * @see #toToml(Object)
 */
@ApiStatus.AvailableSince("1.4.1")
public final class ReflectTomlSerializer<T> implements TomlSerializer.Symmetric<T> {

    static final int C_SERIALIZE   = 1;
    static final int C_DESERIALIZE = 2;

    private static void checkType(
            @NotNull Class<?> type,
            @MagicConstant(flags = { C_SERIALIZE, C_DESERIALIZE }) int capabilities
    ) throws IllegalArgumentException {
        if (RecordSupport.isRecord(type)) return;
        if (TomlSerializable.class.isAssignableFrom(type)) {
            if ((capabilities & C_SERIALIZE) == 0) return;
            int mod = type.getModifiers();
            if (Modifier.isInterface(mod) || Modifier.isAbstract(mod)) {
                raiseTypeError(type, capabilities, "not directly instantiable");
            }
        } else {
            raiseTypeError(type, capabilities, "does not implement TomlSerializable and is not a record");
        }
    }

    @Contract("_, _, _ -> fail")
    private static void raiseTypeError(
            @NotNull Class<?> type,
            @MagicConstant(flags = { C_SERIALIZE, C_DESERIALIZE }) int capabilities,
            @NotNull String detail
    ) {
        String classifier = ((capabilities & C_SERIALIZE) == 0) ? "deserializer" : "serializer";
        throw new IllegalArgumentException("Cannot create " + classifier + " for " + type.getName() +
                " (" + detail + ")");
    }

    //

    private final TableTypeModel<T> model;
    private final TypeAdapters adapters;
    private final int capabilities;

    /**
     * Create a new serializer for converting objects of the given
     * type to/from a TOML table. The newly created serializer will
     * use the {@link TypeAdapters#standard() standard set} of adapters.
     * @param type The table-like type to convert to/from.
     * @throws IllegalArgumentException The given type is not serializable.
     * @see #ReflectTomlSerializer(Class, TypeAdapters)
     */
    public ReflectTomlSerializer(
            @NotNull Class<T> type
    ) throws IllegalArgumentException {
        this(type, TypeAdapters.standard());
    }

    /**
     * Create a new serializer for converting objects of the given
     * type to/from a TOML table.
     * @param type The table-like type to convert to/from.
     * @param adapters Describes how leaves in the document tree may be converted to/from Java objects.
     * @throws IllegalArgumentException The given type is not serializable.
     * @see #ReflectTomlSerializer(Class)
     */
    public ReflectTomlSerializer(
            @NotNull Class<T> type,
            @NotNull TypeAdapters adapters
    ) throws IllegalArgumentException {
        this(type, adapters, C_SERIALIZE | C_DESERIALIZE);
    }

    ReflectTomlSerializer(
            @NotNull Class<T> type,
            @NotNull TypeAdapters adapters,
            @MagicConstant(flags = { C_SERIALIZE, C_DESERIALIZE }) int capabilities
    ) {
        checkType(type, capabilities);
        TableTypeModel<T> model = TableTypeModel.match(new ParameterizedClass<>(type));
        assert model != null;
        this.model = model;
        this.adapters = adapters;
        this.capabilities = capabilities;
    }

    //

    @Override
    public @NotNull Class<T> serialType() {
        return this.model.type();
    }

    @Override
    public @NotNull T fromToml(@NotNull TomlTable table) {
        if ((this.capabilities & C_SERIALIZE) == 0) throw new UnsupportedOperationException();
        return this.serializeTable(
                this.model,
                table
        );
    }

    @Override
    public @NotNull TomlTable toToml(@NotNull T data) {
        if ((this.capabilities & C_DESERIALIZE) == 0) throw new UnsupportedOperationException();
        ReferenceHolder parents = new ReferenceHolder();
        parents.add(this);
        return this.deserializeTable(
                parents,
                this.model,
                data
        );
    }

    @Override
    public @NotNull T serialize(@NotNull TomlTable table) {
        return this.fromToml(table);
    }

    @Override
    public @NotNull TomlTable deserialize(@NotNull T data) {
        return this.toToml(data);
    }

    //

    private <E> @NotNull E serializeValue(
            @NotNull TypeModel<E> model,
            @NotNull TomlValue value
    ) {
        TypeAdapter<E> adapter = this.adapters.get(model.type());
        if (adapter != null) return adapter.toJava(value.asPrimitive());
        if (model.isArray()) return this.serializeArray(model.asArray(), value.asArray());
        if (model.isTable()) return this.serializeTable(model.asTable(), value.asTable());
        throw new IllegalArgumentException("No adapter for type " + model.type().getName());
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

        TypeAdapter<E> adapter = this.adapters.get(model.type());
        if (adapter != null) return adapter.toToml(value);
        if (model.isArray()) return this.deserializeArray(parents, model.asArray(), value);
        if (model.isTable()) return this.deserializeTable(parents, model.asTable(), value);

        throw new IllegalArgumentException("No adapter for type " + model.type().getName());
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
