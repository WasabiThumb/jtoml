package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

@ApiStatus.Internal
final class StringMapTableTypeModel<T extends Map<String, V>, V> extends AbstractTableTypeModel<T> {

    @SuppressWarnings("unchecked")
    static <IT extends Map<String, IV>, IV> StringMapTableTypeModel<?, ?> create(
            @NotNull Class<IT> mapClass,
            @NotNull ParameterizedClass<?> valueType
    ) {
        return new StringMapTableTypeModel<>(mapClass, (ParameterizedClass<IV>) valueType);
    }

    //

    private final Class<T> clazz;
    private final ParameterizedClass<V> valueType;

    private StringMapTableTypeModel(@NotNull Class<T> clazz, @NotNull ParameterizedClass<V> valueType) {
        this.clazz = clazz;
        this.valueType = valueType;
    }

    //

    @Override
    public @NotNull Class<T> type() {
        return this.clazz;
    }

    @Override
    public @NotNull TableTypeModel.Builder<T> create() {
        T map;
        if (this.clazz.isAssignableFrom(HashMap.class)) {
            map = this.clazz.cast(new HashMap<String, V>());
        } else {
            Object o;
            try {
                o = this.clazz.getConstructor()
                        .newInstance();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Map class " + this.clazz.getName() + " has no primary constructor");
            } catch (ReflectiveOperationException | SecurityException e) {
                throw new IllegalStateException("Unexpected reflection error", e);
            }
            map = this.clazz.cast(o);
        }
        return new StringMapTableTypeModel.Builder<>(this, map);
    }

    @Override
    public @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull T instance) {
        Set<String> keys = instance.keySet();
        List<TomlKey> ret = new ArrayList<>(keys.size());
        for (String key : keys) ret.add(TomlKey.literal(key));
        return Collections.unmodifiableList(ret);
    }

    @Override
    public @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key) {
        return this.valueType;
    }

    @Override
    public @UnknownNullability Object get(@NotNull T instance, @NotNull TomlKey key) {
        if (key.size() != 1)
            throw new IllegalArgumentException("Illegal key size (expected 1, got " + key.size() + ")");

        return instance.get(key.get(0));
    }

    //

    private static final class Builder<T extends Map<String, V>, V> implements TableTypeModel.Builder<T> {

        private final StringMapTableTypeModel<T, V> parent;
        private final T map;

        private Builder(@NotNull StringMapTableTypeModel<T, V> parent, @NotNull T map) {
            this.parent = parent;
            this.map = map;
        }

        //

        @Override
        public void set(@NotNull TomlKey key, @NotNull Object value) {
            if (key.size() != 1)
                throw new IllegalArgumentException("Illegal key size (expected 1, got " + key.size() + ")");

            this.map.put(key.get(0), this.parent.valueType.raw().cast(value));
        }

        @Override
        public @NotNull T build() {
            return this.map;
        }

    }

}
