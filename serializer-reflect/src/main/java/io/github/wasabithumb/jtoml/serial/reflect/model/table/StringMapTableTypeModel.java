/*
 * Copyright 2026 Xavier Pedraza
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

@ApiStatus.Internal
final class StringMapTableTypeModel<T extends Map<String, V>, V> extends AbstractTableTypeModel<T> {

    @SuppressWarnings("unchecked")
    static <IT extends Map<String, IV>, IV> StringMapTableTypeModel<?, ?> create(
            Class<IT> mapClass,
            ParameterizedClass<?> valueType
    ) {
        return new StringMapTableTypeModel<>(mapClass, (ParameterizedClass<IV>) valueType);
    }

    private static Key stringKey(String value) {
        return new StringKey(value);
    }

    private static String unwrapStringKey(Key key) throws IllegalArgumentException {
        if (key instanceof StringKey) {
            return ((StringKey) key).value;
        }
        throw new IllegalArgumentException("Key " + key + " is not a StringKey");
    }
    
    //

    private final Class<T> clazz;
    private final ParameterizedClass<V> valueType;

    private StringMapTableTypeModel(Class<T> clazz, ParameterizedClass<V> valueType) {
        this.clazz = clazz;
        this.valueType = valueType;
    }

    //

    @Override
    public Class<T> type() {
        return this.clazz;
    }

    @Override
    public TableTypeModel.Builder<T> create() {
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
    public Mapper mapper(KeyConvention defaultConvention) {
        return StringMapper.INSTANCE;
    }

    @Override
    public @Unmodifiable Collection<Key> keys(T instance, KeyConvention convention) {
        Set<String> keys = instance.keySet();
        List<Key> ret = new ArrayList<>(keys.size());
        for (String key : keys) ret.add(stringKey(key));
        return Collections.unmodifiableList(ret);
    }

    @Override
    public ParameterizedClass<?> elementType(Key ignored) {
        return this.valueType;
    }

    @Override
    public @UnknownNullability Object get(T instance, Key key) {
        return instance.get(unwrapStringKey(key));
    }

    //

    private static final class Builder<T extends Map<String, V>, V> implements TableTypeModel.Builder<T> {

        private final StringMapTableTypeModel<T, V> parent;
        private final T map;

        private Builder(StringMapTableTypeModel<T, V> parent, T map) {
            this.parent = parent;
            this.map = map;
        }

        //

        @Override
        public void set(Key key, Object value) {
            this.map.put(unwrapStringKey(key), this.parent.valueType.raw().cast(value));
        }

        @Override
        public T build() {
            return this.map;
        }

    }

    private static final class StringKey extends AbstractKey {

        private final String value;

        StringKey(String value) {
            this.value = value;
        }

        //

        @Override
        public TomlKey asTomlKey() {
            return TomlKey.literal(this.value);
        }

    }

    private static final class StringMapper implements Mapper {

        static final StringMapper INSTANCE = new StringMapper();

        @Override
        public Key fromTomlKey(TomlKey key) {
            if (key.size() != 1) throw new IllegalStateException("TOML key should have 1 part when associated with string map (got " + key + ")");
            return new StringKey(key.get(0));
        }

    }

}
