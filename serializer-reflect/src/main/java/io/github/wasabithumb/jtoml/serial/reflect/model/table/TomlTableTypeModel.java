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
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.*;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

@ApiStatus.Internal
final class TomlTableTypeModel extends AbstractTableTypeModel<TomlTable> {

    static final TomlTableTypeModel INSTANCE = new TomlTableTypeModel();

    //

    @Override
    public @NotNull Class<TomlTable> type() {
        return TomlTable.class;
    }

    @Override
    public @NotNull TableTypeModel.Builder<TomlTable> create() {
        return new Builder();
    }

    @Override
    public @NotNull Mapper mapper(@NotNull KeyConvention defaultConvention) {
        return LiteralMapper.INSTANCE;
    }

    @Override
    public @NotNull @Unmodifiable Collection<Key> keys(@NotNull TomlTable instance, @NotNull KeyConvention ignored) {
        return new KeySet(instance.keys(false));
    }

    @Override
    public @NotNull ParameterizedClass<?> elementType(@NotNull Key key) {
        return new ParameterizedClass<>(TomlValue.class);
    }

    @Override
    public @UnknownNullability Object get(@NotNull TomlTable instance, @NotNull Key key) {
        return instance.get(key.asTomlKey());
    }

    //

    private static final class Builder implements TableTypeModel.Builder<TomlTable> {

        private final TomlTable table = TomlTable.create();

        @Override
        public void set(@NotNull Key key, @NotNull Object value) {
            this.table.put(key.asTomlKey(), (TomlValue) value);
        }

        @Override
        public @NotNull TomlTable build() {
            return this.table;
        }

    }

    private static final class LiteralKey extends AbstractKey {

        private final TomlKey value;

        LiteralKey(@NotNull TomlKey value) {
            this.value = value;
        }

        //

        @Override
        public @NotNull TomlKey asTomlKey() {
            return this.value;
        }

    }

    private static final class LiteralMapper implements Mapper {

        private static final LiteralMapper INSTANCE = new LiteralMapper();

        @Override
        public @NotNull Key fromTomlKey(@NotNull TomlKey key) {
            return new LiteralKey(key);
        }

    }

    private static final class KeySet extends AbstractSet<Key> {

        private final Set<TomlKey> backing;

        KeySet(@NotNull Set<TomlKey> backing) {
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
