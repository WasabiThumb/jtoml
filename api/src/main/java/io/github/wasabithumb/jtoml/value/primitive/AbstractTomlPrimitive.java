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

package io.github.wasabithumb.jtoml.value.primitive;

import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.ZoneOffset;

@ApiStatus.Internal
abstract class AbstractTomlPrimitive<T extends Serializable> implements TomlPrimitive {

    protected final long creationTime;
    protected final Comments comments;
    protected transient byte flags;

    protected AbstractTomlPrimitive(@NotNull Comments comments) {
        this.creationTime = System.nanoTime();
        this.comments = comments;
        this.flags = (byte) 0;
    }

    //

    @Override
    public long creationTime() {
        return this.creationTime;
    }

    @Override
    public int flags() {
        return this.flags & 0xFF;
    }

    @Override
    public @NotNull TomlPrimitive flags(int flags) {
        this.flags = (byte) flags;
        return this;
    }

    @Override
    public @NotNull Comments comments() {
        return this.comments;
    }

    @Override
    public abstract @NotNull T value();

    @ApiStatus.Internal
    @NotNull ZoneOffset temporalOffset() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Primitive has no temporal offset");
    }

    //

    @Override
    public int hashCode() {
        int h = 7;
        h = 31 * h + this.type().hashCode();
        h = 31 * h + this.value().hashCode();
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TomlPrimitive)) return false;
        TomlPrimitive other = (TomlPrimitive) obj;
        if (this.type() != other.type()) return false;
        return this.value().equals(other.value());
    }

    @Override
    public @NotNull String toString() {
        return "TomlPrimitive[type=" + this.type().name() + ", value=" + this.value() + "]";
    }

}
