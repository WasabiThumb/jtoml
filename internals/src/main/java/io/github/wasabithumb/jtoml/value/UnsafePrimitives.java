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

package io.github.wasabithumb.jtoml.value;

import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Experimental
public final class UnsafePrimitives {

    private static final Constructor<?> FLOAT_WITH_CHARS;
    private static final Class<?> TEMPORAL_CLASS;
    private static final Field TEMPORAL_MIN_NANO_RESOLUTION;
    static {
        Constructor<?> floatWithChars = null;
        Class<?> temporalClass = null;
        Field temporalMinNanoResolution = null;
        try {
            Class<?> clsFloatTomlPrimitive = Class.forName("io.github.wasabithumb.jtoml.value.primitive.FloatTomlPrimitive");
            floatWithChars = clsFloatTomlPrimitive.getDeclaredConstructor(Double.TYPE, String.class);
            floatWithChars.setAccessible(true);
            temporalClass = Class.forName("io.github.wasabithumb.jtoml.value.primitive.AbstractTemporalTomlPrimitive");
            temporalMinNanoResolution = temporalClass.getDeclaredField("minNanoResolution");
            temporalMinNanoResolution.setAccessible(true);
        } catch (Exception e) {
            Logger.getLogger("jtoml")
                    .log(Level.WARNING, "Failed to access internals (please report this)", e);
        }
        FLOAT_WITH_CHARS = floatWithChars;
        TEMPORAL_CLASS = temporalClass;
        TEMPORAL_MIN_NANO_RESOLUTION = temporalMinNanoResolution;
    }

    //

    @Contract("_, _ -> new")
    public static @NotNull TomlPrimitive createFloat(double v, @NotNull String chars) {
        if (FLOAT_WITH_CHARS == null) return TomlPrimitive.of(v);
        TomlPrimitive ret;
        try {
            ret = (TomlPrimitive) FLOAT_WITH_CHARS.newInstance(v, chars);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IllegalStateException("Primitive constructor raised a checked exception", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected reflection error", e);
        }
        return ret;
    }

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
