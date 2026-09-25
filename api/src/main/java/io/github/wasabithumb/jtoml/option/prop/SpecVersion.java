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

package io.github.wasabithumb.jtoml.option.prop;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An enum representing versions of the TOML
 * specification recognized by JToml.
 * @see #V1_0_0
 * @see #V1_1_0
 */
@ApiStatus.AvailableSince("1.4.0")
public enum SpecVersion {
    /**
     * Version <a href="https://toml.io/en/v1.0.0">1.0.0</a>
     */
    V1_0_0(0),

    /**
     * Version <a href="https://toml.io/en/v1.1.0">1.1.0</a>
     */
    V1_1_0(1);

    //

    /**
     * Provides the latest version of the specification
     * supported by this library.
     */
    public static @NotNull SpecVersion latest() {
        return V1_1_0;
    }

    //

    private final int minor;

    SpecVersion(int minor) {
        this.minor = minor;
    }

    //

    /**
     * Utility for determining feature support
     * for the target {@link SpecVersion}.
     * @param major The required major version number.
     * @param minor The required minor version number.
     * @return True if this {@link SpecVersion} represents a version
     *         that is greater than or equal to the version represented by
     *         {@code <major>.<minor>}.
     */
    public boolean isAtLeast(int major, int minor) {
        if (major > 1) return false;
        return minor <= this.minor;
    }

}
