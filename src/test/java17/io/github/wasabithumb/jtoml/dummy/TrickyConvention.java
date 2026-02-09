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

import io.github.wasabithumb.jtoml.Faker;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;
import io.github.wasabithumb.jtoml.serial.reflect.Key;

public final class TrickyConvention implements TomlSerializable {

    public static TrickyConvention create() {
        return Faker.create(TrickyConvention.class);
    }

    //

    @Convention.Literal public int literalInt;
    @Convention.Lower public int lowerInt;
    public int defaultingInt;
    @Convention.Kebab public int kebabInt;
    @Convention.Split public int splitInt;
    @Key("custom") public int customInt;

    //

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrickyConvention other)) return false;
        return this.literalInt == other.literalInt &&
                this.lowerInt == other.lowerInt &&
                this.defaultingInt == other.defaultingInt &&
                this.kebabInt == other.kebabInt &&
                this.splitInt == other.splitInt &&
                this.customInt == other.customInt;
    }

}
