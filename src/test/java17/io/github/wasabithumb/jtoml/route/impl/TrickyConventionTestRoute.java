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

package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.key.convention.KeyConvention;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;
import io.github.wasabithumb.jtoml.serial.reflect.Key;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TrickyConventionTestRoute implements TestRoute.Configuring {

    @Sentinel("tricky-convention.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Tricky Conventions";
    }

    @Override
    public void execute(JToml instance) {
        final Document d = this.document;
        assertEquals(1, d.literalInt);
        assertEquals(2, d.lowerInt);
        assertEquals(3, d.snakeInt);
        assertEquals(4, d.kebabInt);
        assertEquals(5, d.splitInt);
        assertEquals(6, d.custom);
    }

    @Override
    public void configure(JTomlOptions.Builder options) {
        options.set(JTomlOption.DEFAULT_KEY_CONVENTION, FailingKeyConvention.INSTANCE);
    }

    //

    @Convention.Snake
    private record Document(
            @Convention.Literal int literalInt,
            @Convention.Lower int lowerInt,
            int snakeInt,
            @Convention.Kebab int kebabInt,
            @Convention.Split int splitInt,
            @Key("custom") int custom
    ) { }

    /** Used to ensure that all the explicitly defined conventions are actually used */
    private static final class FailingKeyConvention implements KeyConvention {

        static final FailingKeyConvention INSTANCE = new FailingKeyConvention();

        @Override
        public TomlKey toToml(String key) {
            throw new IllegalStateException("Incorrect convention used to resolve key: " + key);
        }

    }

}
