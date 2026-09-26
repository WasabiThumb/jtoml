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

package io.github.wasabithumb.jtoml.document;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@NullMarked
@ApiStatus.Internal
public final class TomlDocumentImpl implements TomlDocument {

    private final long creationTime;
    private final TomlTable backing;
    private final TomlIssues issues;
    private boolean orderMarked = false;

    public TomlDocumentImpl(
            TomlTable backing,
            TomlIssues issues
    ) {
        this.creationTime = System.nanoTime();
        this.backing = backing;
        this.issues = issues;
    }

    //

    @Override
    public @Unmodifiable TomlIssues issues() {
        return this.issues;
    }

    public boolean isOrderMarked() {
        return this.orderMarked;
    }

    public void setOrderMarked(boolean orderMarked) {
        this.orderMarked = orderMarked;
    }

    @Override
    public long creationTime() {
        return this.creationTime;
    }

    @Override
    public int flags() {
        return this.backing.flags();
    }

    @Override
    public TomlDocument flags(int flags) {
        this.backing.flags(flags);
        return this;
    }

    @Override
    public Comments comments() {
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
    public @Unmodifiable Set<TomlKey> keys(boolean deep) {
        return this.backing.keys(deep);
    }

    @Override
    public boolean contains(TomlKey key) {
        return this.backing.contains(key);
    }

    @Override
    public @Nullable TomlValue get(TomlKey key) {
        return this.backing.get(key);
    }

    @Override
    public @Nullable TomlValue put(TomlKey key, TomlValue value) {
        return this.backing.put(key, value);
    }

    @Override
    public @Nullable TomlValue remove(TomlKey key) {
        return this.backing.remove(key);
    }

}
