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

package io.github.wasabithumb.jtoml.test;

import io.github.wasabithumb.jtoml.value.table.TomlTable;

final class InvalidTestSpec extends AbstractTestSpec {

    public InvalidTestSpec(String name) {
        super(name);
    }

    //

    @Override
    public boolean shouldFail() {
        return true;
    }

    @Override
    public void validate(TomlTable table) {
        throw new UnsupportedOperationException();
    }

}
