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

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
public final class TomlValueFlags {

    private static final int F_CONSTANT = 1;
    private static final int F_NON_REUSABLE = 2;
    private static final int F_NON_KEY_EXTENDABLE = 4;

    //

    private static boolean get(
            TomlValue v,
            @MagicConstant(valuesFromClass = TomlValueFlags.class) int flag
    ) {
        return (v.flags() & flag) != 0;
    }

    @Contract("_, _, _ -> param1")
    private static TomlValue set(
            TomlValue v,
            @MagicConstant(valuesFromClass = TomlValueFlags.class) int flag,
            boolean value
    ) {
        int n = v.flags();
        if (value) {
            n |= flag;
        } else {
            n &= (~flag);
        }
        return v.flags(n);
    }

    //

    public static boolean isConstant(TomlValue v) {
        return get(v, F_CONSTANT);
    }

    @Contract("_, _ -> param1")
    public static TomlValue setConstant(TomlValue v, boolean constant) {
        return set(v, F_CONSTANT, constant);
    }

    public static boolean isNonReusable(TomlValue v) {
        return get(v, F_NON_REUSABLE);
    }

    @Contract("_, _ -> param1")
    public static TomlValue setNonReusable(TomlValue v, boolean nonReusable) {
        return set(v, F_NON_REUSABLE, nonReusable);
    }

    public static boolean isNonKeyExtendable(TomlValue v) {
        return get(v, F_NON_KEY_EXTENDABLE);
    }

    @Contract("_, _ -> param1")
    public static TomlValue setNonKeyExtendable(TomlValue v, boolean nonKeyExtendable) {
        return set(v, F_NON_KEY_EXTENDABLE, nonKeyExtendable);
    }

    //

    private TomlValueFlags() {
        throw new IllegalStateException();
    }

}
