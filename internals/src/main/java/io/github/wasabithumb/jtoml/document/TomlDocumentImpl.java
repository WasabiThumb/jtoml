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

package io.github.wasabithumb.jtoml.document;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

@ApiStatus.Internal
public final class TomlDocumentImpl implements TomlDocument {

    private final long creationTime;
    private final TomlTable backing;
    private boolean orderMarked = false;

    public TomlDocumentImpl(@NotNull TomlTable backing) {
        this.creationTime = System.nanoTime();
        this.backing = backing;
    }

    // START Metadata

    public boolean isOrderMarked() {
        return this.orderMarked;
    }

    public void setOrderMarked(boolean orderMarked) {
        this.orderMarked = orderMarked;
    }

    // END Metadata

    // START Super

    @Override
    public long creationTime() {
        return this.creationTime;
    }

    @Override
    public int flags() {
        return this.backing.flags();
    }

    @Override
    public @NotNull TomlDocument flags(int flags) {
        this.backing.flags(flags);
        return this;
    }

    @Override
    public @NotNull Comments comments() {
        return this.backing.comments();
    }

    @Override
    public int size() {
        return this.backing.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backing.isEmpty();
    }

    @Override
    public void clear() {
        this.backing.clear();
    }

    @Override
    public @NotNull @Unmodifiable Set<TomlKey> keys(boolean deep) {
        return this.backing.keys(deep);
    }

    @Override
    public boolean contains(@NotNull TomlKey key) {
        return this.backing.contains(key);
    }

    @Override
    public @Nullable TomlValue get(@NotNull TomlKey key) {
        return this.backing.get(key);
    }

    @Override
    public @Nullable TomlValue put(@NotNull TomlKey key, @NotNull TomlValue value) {
        return this.backing.put(key, value);
    }

    @Override
    public @Nullable TomlValue remove(@NotNull TomlKey key) {
        return this.backing.remove(key);
    }

    // END Super

}
