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

package io.github.wasabithumb.jtoml;

import io.github.wasabithumb.jtoml.option.JTomlOptions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Responsible for finding the
 * {@code JTomlImpl} class within this
 * package, exposing its constructor
 * and immediately invoking it to produce
 * a default instance. When multiple instances
 * of JToml exist (ideally relocated), each
 * instance will have its own
 * {@code JTomlProvider} with its own
 * {@code JTomlImpl} singleton.
 */
@ApiStatus.Internal
final class JTomlProvider {

    private static final JTomlOptions DEFAULT_OPTIONS = JTomlOptions.defaults();
    private static boolean INIT = false;
    private static @UnknownNullability JTomlProvider INSTANCE;

    public static synchronized JTomlProvider get() {
        if (INIT) return INSTANCE;
        JTomlProvider ret = new JTomlProvider(findImplCtor());
        INSTANCE = ret;
        INIT = true;
        return ret;
    }

    private static MethodHandle findImplCtor() {
        String packageName = JToml.class.getPackage().getName();
        String className = packageName + ".JTomlImpl";
        Class<?> cls;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JTomlImpl not found in " + packageName + " (api-only classpath)");
        }
        try {
            return MethodHandles.lookup()
                    .findConstructor(cls, MethodType.methodType(Void.TYPE, JTomlOptions.class));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot access primary constructor for JTomlImpl", e);
        }
    }

    //

    private final MethodHandle ctor;
    private final JToml defaultInstance;

    private JTomlProvider(
            MethodHandle ctor
    ) {
        this.ctor = ctor;
        this.defaultInstance = newInstance(ctor, DEFAULT_OPTIONS);
    }

    //

    @Contract(pure = true)
    public JToml instance() {
        return this.defaultInstance;
    }

    @Contract("_ -> new")
    public JToml instance(JTomlOptions options) {
        if (DEFAULT_OPTIONS.equals(options)) return this.defaultInstance;
        return newInstance(this.ctor, options);
    }

    private static JToml newInstance(MethodHandle ctor, JTomlOptions options) {
        try {
            return (JToml) ctor.invoke(options);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new IllegalStateException("JTomlImpl constructor raised a checked exception", t);
        }
    }

}
