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

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.parse.TomlLocalParseException;
import io.github.wasabithumb.jtoml.except.parse.TomlMultiParseException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.*;

@ApiStatus.Internal
final class TomlIssuesImpl
        extends AbstractList<TomlIssue>
        implements TomlIssues
{

    public static final TomlIssues EMPTY = new TomlIssuesImpl(new TomlIssue[0]);

    //

    private final TomlIssue[] array;

    public TomlIssuesImpl(TomlIssue[] array) {
        this.array = array;
    }

    //

    @Override
    public int size() {
        return this.array.length;
    }

    @Override
    public TomlIssue get(int index) {
        if (index < 0 || index >= this.array.length)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + this.array.length);
        return this.array[index];
    }

    @Override
    public void unwrap() throws TomlException {
        int len = this.array.length;
        if (len == 0) return;
        if (len == 1) throw this.array[0].toException();
        List<TomlLocalParseException> ex = new ArrayList<>(len);
        for (TomlIssue issue : this.array) ex.add(issue.toException());
        throw TomlMultiParseException.create(ex);
    }

    @Override
    @Contract(" -> new")
    public Builder toBuilder() {
        Builder ret = new Builder();
        ret.set.addAll(Arrays.asList(this.array));
        return ret;
    }

    //

    static final class Builder implements TomlIssues.Builder {

        private final Set<TomlIssue> set = new LinkedHashSet<>();

        //

        @Override
        public Builder add(TomlIssue issue) {
            this.set.add(issue);
            return this;
        }

        @Override
        public TomlIssuesImpl build() {
            final int len = this.set.size();
            final TomlIssue[] array = new TomlIssue[len];
            Iterator<TomlIssue> iter = set.iterator();
            for (int i = 0; i < len; i++) {
                if (!iter.hasNext()) throw new ConcurrentModificationException();
                array[i] = iter.next();
            }
            return new TomlIssuesImpl(array);
        }

    }

}
