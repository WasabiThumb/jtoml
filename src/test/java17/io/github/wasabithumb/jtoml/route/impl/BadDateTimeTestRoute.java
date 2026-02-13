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
import io.github.wasabithumb.jtoml.except.TomlValueException;
import io.github.wasabithumb.jtoml.route.TestRoute;

import java.time.LocalDate;

public final class BadDateTimeTestRoute implements TestRoute.Failing<TomlValueException> {

    @Override
    public String displayName() {
        return "Bad Date-Time";
    }

    @Override
    public Class<TomlValueException> errorType() {
        return TomlValueException.class;
    }

    @Override
    public void executeFailing(JToml instance) throws TomlValueException {
        DateTable table = new DateTable(
                LocalDate.now(),
                LocalDate.of(-1, 7, 16) // year -1
        );
        instance.toToml(table);
    }

    //

    private record DateTable(
            LocalDate valid,
            LocalDate invalid
    ) { }

}
