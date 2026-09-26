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
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

@ApiStatus.Internal
final class TomlTableTypeModel extends AbstractTableTypeModel<TomlTable> {

    static final TomlTableTypeModel INSTANCE = new TomlTableTypeModel();

    //

    @Override
    public Class<TomlTable> type() {
        return TomlTable.class;
    }

    @Override
    public TableTypeModel.Builder<TomlTable> create() {
        return new Builder();
    }

    @Override
    public Mapper mapper(KeyConvention defaultConvention) {
        return LiteralMapper.INSTANCE;
    }

    @Override
    public @Unmodifiable Collection<Key> keys(TomlTable instance, KeyConvention ignored) {
        return new KeySet(instance.keys(false));
    }

    @Override
    public ParameterizedClass<?> elementType(Key key) {
        return new ParameterizedClass<>(TomlValue.class);
    }

    @Override
    public @UnknownNullability Object get(TomlTable instance, Key key) {
        return instance.get(key.asTomlKey());
    }

    //

    private static final class Builder implements TableTypeModel.Builder<TomlTable> {

        private final TomlTable table = TomlTable.create();

        @Override
        public void set(Key key, Object value) {
            this.table.put(key.asTomlKey(), (TomlValue) value);
        }

        @Override
        public TomlTable build() {
            return this.table;
        }

    }

    private static final class LiteralKey extends AbstractKey {

        private final TomlKey value;

        LiteralKey(TomlKey value) {
            this.value = value;
        }

        //

        @Override
        public TomlKey asTomlKey() {
            return this.value;
        }

    }

    private static final class LiteralMapper implements Mapper {

        private static final LiteralMapper INSTANCE = new LiteralMapper();

        @Override
        public Key fromTomlKey(TomlKey key) {
            return new LiteralKey(key);
        }

    }

    private static final class KeySet extends AbstractSet<Key> {

        private final Set<TomlKey> backing;

        KeySet(Set<TomlKey> backing) {
            this.backing = backing;
        }

        //

        @Override
        public int size() {
            return this.backing.size();
        }

        @Override
        public Iterator<Key> iterator() {
            return this.backing.stream()
                    .map((TomlKey tk) -> (Key) new LiteralKey(tk))
                    .iterator();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Key)) return false;
            return this.backing.contains(((Key) o).asTomlKey());
        }

    }

}
