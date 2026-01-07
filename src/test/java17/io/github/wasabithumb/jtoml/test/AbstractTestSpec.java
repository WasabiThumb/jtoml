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

import java.io.IOException;
import java.io.InputStream;

abstract class AbstractTestSpec implements TestSpec {

    protected final String name;

    public AbstractTestSpec(String name) {
        this.name = name;
    }

    //

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public InputStream read() throws IOException {
        return this.open("/tests/" + this.name + ".toml");
    }

    protected InputStream open(String path) throws IOException {
        InputStream is = TestSpec.class.getResourceAsStream(path);
        if (is == null) throw new IOException("Resource \"" + path + "\" not found");
        return is;
    }

}
