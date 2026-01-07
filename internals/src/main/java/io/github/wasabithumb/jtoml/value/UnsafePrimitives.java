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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Experimental
public final class UnsafePrimitives {

    private static final Constructor<?> FLOAT_WITH_CHARS;
    static {
        Class<?> cls;
        Constructor<?> con = null;
        try {
            cls = Class.forName("io.github.wasabithumb.jtoml.value.primitive.FloatTomlPrimitive");
            con = cls.getDeclaredConstructor(Double.TYPE, String.class);
            con.setAccessible(true);
        } catch (ReflectiveOperationException | SecurityException e) {
            Logger.getLogger("jtoml")
                    .log(Level.WARNING, "Failed to access float constructor (please report this)", e);
        }
        FLOAT_WITH_CHARS = con;
    }

    //

    @Contract("_, _ -> new")
    public static @NotNull TomlPrimitive createFloat(double v, @NotNull String chars) {
        if (FLOAT_WITH_CHARS == null) return TomlPrimitive.of(v);
        TomlPrimitive ret;
        try {
            ret = (TomlPrimitive) FLOAT_WITH_CHARS.newInstance(v, chars);
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw ((RuntimeException) cause);
            throw new AssertionError("Unexpected error in constructor/initializer", e);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unexpected reflection error", e);
        }
        return ret;
    }

    //

    private UnsafePrimitives() { }

}
