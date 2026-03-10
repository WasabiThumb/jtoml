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
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.helper.HolidaysTestRoute;

import java.util.List;
import java.util.Map;

public final class ListHolidaysTestRoute extends HolidaysTestRoute {

    @Sentinel("holidays.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Holidays to Lists";
    }

    @Override
    public void execute(JToml instance) {
        validateDocument(this.document.months, this.document.holidays);
    }

    //

    private record Document(
            List<Month> months,
            Map<String, List<Holiday>> holidays
    ) { }

}
