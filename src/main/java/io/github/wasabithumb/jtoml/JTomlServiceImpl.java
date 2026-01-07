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

package io.github.wasabithumb.jtoml;

import io.github.wasabithumb.jtoml.option.JTomlOptions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class JTomlServiceImpl extends JTomlService {

    private static final JTomlImpl WITH_DEFAULTS = new JTomlImpl(JTomlOptions.defaults());

    public JTomlServiceImpl() { }

    @Override
    public @NotNull JToml defaultInstance() {
        return WITH_DEFAULTS;
    }

    @Override
    public @NotNull JToml createInstance(@NotNull JTomlOptions options) {
        return new JTomlImpl(options);
    }

}
