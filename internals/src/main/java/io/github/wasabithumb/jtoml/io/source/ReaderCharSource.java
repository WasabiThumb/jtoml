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

package io.github.wasabithumb.jtoml.io.source;

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import io.github.wasabithumb.jtoml.except.parse.TomlBomException;
import io.github.wasabithumb.jtoml.option.prop.OrderMarkPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public final class ReaderCharSource extends FilterReader implements CharSource {

    private static final int BOM_ALLOWED  = 1;
    private static final int BOM_REQUIRED = 2;
    private static final int BOM_CHECKED  = 4;
    private static final int BOM_READ     = 8;

    @SuppressWarnings("fallthrough")
    private static int initialFlags(@NotNull OrderMarkPolicy bomPolicy) {
        int flags = 0;
        switch (bomPolicy) {
            case ALWAYS:
                flags |= BOM_REQUIRED;
            case IF_PRESENT:
                flags |= BOM_ALLOWED;
                break;
        }
        return flags;
    }

    //

    private int flags;

    public ReaderCharSource(@NotNull Reader in, @NotNull OrderMarkPolicy bomPolicy) {
        super(in);
        this.flags = initialFlags(bomPolicy);
    }

    //

    public boolean didReadBOM() {
        return (this.flags & BOM_READ) != 0;
    }

    private @Range(from = -1, to = 0xFFFF) int readRethrow() throws TomlIOException {
        try {
            return this.in.read();
        } catch (IOException e) {
            TomlIOException.rethrow(e);
            return -1;
        }
    }

    @Override
    public @Range(from = -1, to = 0xFFFF) int next() throws TomlException {
        int c = this.readRethrow();
        if ((this.flags & BOM_CHECKED) == 0) {
            if (c == 0xFEFF) {
                if ((this.flags & BOM_ALLOWED) == 0) {
                    throw new TomlBomException("Source data contains a BOM");
                } else {
                    this.flags |= BOM_READ;
                    c = this.readRethrow();
                }
            } else if ((this.flags & BOM_REQUIRED) != 0) {
                throw new TomlBomException("Source data does not contain a BOM");
            }
        }
        return c;
    }

    @Override
    public void close() throws TomlIOException {
        try {
            super.close();
        } catch (IOException e) {
            TomlIOException.rethrow(e);
        }
    }

}
