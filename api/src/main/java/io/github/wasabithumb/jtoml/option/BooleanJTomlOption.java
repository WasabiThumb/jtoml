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

package io.github.wasabithumb.jtoml.option;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
final class BooleanJTomlOption extends AbstractJTomlOption<Boolean> implements JTomlOption.Bool {

    public static BooleanJTomlOption of(
            String name,
            boolean defaultValue
    ) {
        return new BooleanJTomlOption(name, defaultValue);
    }

    //

    public BooleanJTomlOption(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

}
