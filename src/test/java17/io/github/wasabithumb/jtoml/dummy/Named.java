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

package io.github.wasabithumb.jtoml.dummy;

import io.github.wasabithumb.jtoml.serial.TomlSerializable;

import java.util.Objects;

public final class Named implements TomlSerializable {

    private final String name;

    private Named() {
        this.name = null;
    }

    public Named(String name) {
        this.name = name;
    }

    //

    public String name() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Named other)) return false;
        return Objects.equals(this.name, other.name);
    }

}
