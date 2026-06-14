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
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.route.TestConstants;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.reflect.Defaulting;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CavalierTestRoute implements TestRoute.Configuring {

    @Override
    public String displayName() {
        return "Cavalier";
    }

    @Override
    public void execute(JToml instance) {
        Document document = new Document(TestConstants.LOREM_IPSUM, TestConstants.MEANING_OF_LIFE);
        TomlTable table = instance.toToml(Document.class, document);
        Document parsed = instance.fromToml(Document.class, table);
        assertEquals(document, parsed);
    }

    @Override
    public void configure(JTomlOptions.Builder options) {
        options.set(JTomlOption.IGNORE_SERIALIZABLE_MARKER, true);
        options.set(JTomlOption.PERMIT_UNSAFE, true);
    }

    //

    /**
     * Not marked with TomlSerializable
     * and does not have a no-args constructor
     */
    private static final class Document {

        String text;
        int number;

        Document(String text, int number) {
            this.text = text;
            this.number = number;
        }

        //


        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Document other)) return false;
            return Objects.equals(this.text, other.text) &&
                    this.number == other.number;
        }

    }

}
