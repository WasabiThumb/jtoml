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

package io.github.wasabithumb.jtoml.value;

import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

@NullMarked
@ApiStatus.Experimental
public final class UnsafePrimitives {

    private static final @UnknownNullability Class<?> TEMPORAL_CLASS;
    private static final @UnknownNullability Field TEMPORAL_MIN_NANO_RESOLUTION;
    static {
        Class<?> temporalClass = null;
        Field temporalMinNanoResolution = null;
        try {
            temporalClass = Class.forName("io.github.wasabithumb.jtoml.value.primitive.AbstractTemporalTomlPrimitive");
            temporalMinNanoResolution = temporalClass.getDeclaredField("minNanoResolution");
            temporalMinNanoResolution.setAccessible(true);
        } catch (Exception e) {
            Logger.getLogger("jtoml")
                    .log(Level.WARNING, "Failed to access internals (please report this)", e);
        }
        TEMPORAL_CLASS = temporalClass;
        TEMPORAL_MIN_NANO_RESOLUTION = temporalMinNanoResolution;
    }

    //

    @Contract(mutates = "param1")
    public static void setTemporalMinNanoResolution(
            TomlPrimitive target,
            @Range(from = 1, to = 9) int minNanoResolution
    ) {
        if (TEMPORAL_CLASS == null ||
                TEMPORAL_MIN_NANO_RESOLUTION == null ||
                !TEMPORAL_CLASS.isInstance(target)
        ) return;

        try {
            TEMPORAL_MIN_NANO_RESOLUTION.setInt(target, minNanoResolution);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected reflection error", e);
        }
    }

    //

    private UnsafePrimitives() { }

}
