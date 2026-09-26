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

package io.github.wasabithumb.jtoml.expression;

import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;

public final class KeyValueExpression extends AbstractExpression {

    private final TomlKey key;
    private final TomlValue value;

    public KeyValueExpression(TomlKey key, TomlValue value) {
        this.key = key;
        this.value = value;
    }

    //

    public TomlKey key() {
        return this.key;
    }

    public TomlValue value() {
        return this.value;
    }

    @Override
    public boolean isKeyValue() {
        return true;
    }

}
