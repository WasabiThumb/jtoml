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

package io.github.wasabithumb.jtoml.serial;

import io.github.wasabithumb.jtoml.JToml;
import org.jetbrains.annotations.NotNull;

public abstract class TomlSerializerService {

    public abstract boolean canSerializeTo(@NotNull Class<?> outType);

    public abstract boolean canDeserializeFrom(@NotNull Class<?> inType);

    public abstract <T> @NotNull TomlSerializer<?, T> getSerializer(@NotNull JToml instance, @NotNull Class<T> outType);

    public abstract <T> @NotNull TomlSerializer<T, ?> getDeserializer(@NotNull JToml instance, @NotNull Class<T> inType);

}
