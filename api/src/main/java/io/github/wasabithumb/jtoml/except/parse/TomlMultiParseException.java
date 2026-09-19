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

package io.github.wasabithumb.jtoml.except.parse;

import io.github.wasabithumb.jtoml.document.TomlIssues;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * Used when multiple {@link TomlLocalParseException}s
 * must be raised for any reason. Currently only occurs when
 * {@link TomlIssues#unwrap() unwrapping issues} when more than 1
 * issue is present.
 */
@ApiStatus.AvailableSince("1.7.0")
public final class TomlMultiParseException extends TomlParseException {

    private static final long serialVersionUID = -6004683919942778023L;

    @Contract("_ -> new")
    public static TomlMultiParseException create(@NotNull Collection<? extends TomlLocalParseException> sub) {
        final int count = sub.size();
        final String message = "Encountered " + count + " distinct issues during TOML parsing";
        final TomlMultiParseException ret = new TomlMultiParseException(message);

        Iterator<? extends TomlLocalParseException> iter = sub.iterator();
        for (int i = 0; i < count; i++) {
            if (!iter.hasNext()) throw new ConcurrentModificationException();
            TomlLocalParseException next = iter.next();
            ret.addSuppressed(next);
        }

        return ret;
    }

    //

    private TomlMultiParseException(@NotNull String message) {
        super(message);
    }

}
