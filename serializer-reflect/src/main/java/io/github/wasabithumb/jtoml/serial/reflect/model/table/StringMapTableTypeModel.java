/*
 * Copyright 2025 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.key.convention.KeyConvention;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.*;

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

    private static @NotNull Key stringKey(@NotNull String value) {
        return new StringKey(value);
    }

    private static @NotNull String unwrapStringKey(@NotNull Key key) throws IllegalArgumentException {
        if (key instanceof StringKey) {
            return ((StringKey) key).value;
        }
        throw new IllegalArgumentException("Key " + key + " is not a StringKey");
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
    public @NotNull Mapper mapper(@NotNull KeyConvention defaultConvention) {
        return StringMapper.INSTANCE;
    }

    @Override
    public @NotNull @Unmodifiable Collection<Key> keys(@NotNull T instance, @NotNull KeyConvention convention) {
        Set<String> keys = instance.keySet();
        List<Key> ret = new ArrayList<>(keys.size());
        for (String key : keys) ret.add(stringKey(key));
        return Collections.unmodifiableList(ret);
    }

    @Override
    public @NotNull ParameterizedClass<?> elementType(@NotNull Key ignored) {
        return this.valueType;
    }

    @Override
    public @UnknownNullability Object get(@NotNull T instance, @NotNull Key key) {
        return instance.get(unwrapStringKey(key));
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
        public void set(@NotNull Key key, @NotNull Object value) {
            this.map.put(unwrapStringKey(key), this.parent.valueType.raw().cast(value));
        }

        @Override
        public @NotNull T build() {
            return this.map;
        }

    }

    private static final class StringKey extends AbstractKey {

        private final String value;

        StringKey(@NotNull String value) {
            this.value = value;
        }

        //

        @Override
        public @NotNull TomlKey asTomlKey() {
            return TomlKey.literal(this.value);
        }

    }

    private static final class StringMapper implements Mapper {

        static final StringMapper INSTANCE = new StringMapper();

        @Override
        public @NotNull Key fromTomlKey(@NotNull TomlKey key) {
            if (key.size() != 1) throw new IllegalStateException("TOML key should have 1 part when associated with string map (got " + key + ")");
            return new StringKey(key.get(0));
        }

    }

}
