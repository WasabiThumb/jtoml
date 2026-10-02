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

package io.github.wasabithumb.jtoml.meta;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@ApiStatus.Internal
public final class JTomlVersionInfo {

    private static @UnknownNullability Properties META;

    private static InputStream openMetaStream() throws IOException {
        InputStream in = JTomlVersionInfo.class.getResourceAsStream("/META-INF/jtoml/meta.properties");
        if (in == null) throw new IllegalStateException("Failed to locate meta.properties");
        return in;
    }

    private static synchronized Properties getMeta() {
        Properties meta = META;
        if (meta != null) return meta;
        meta = new Properties();
        try (InputStream in = openMetaStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
        ) {
            meta.load(r);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse meta.properties");
        }
        META = meta;
        return meta;
    }

    private static String getMetaProperty(@Property String property) {
        Properties meta = getMeta();
        String value = meta.getProperty(property);
        return value == null ? "unknown" : value;
    }


    public static String derivedVersion() {
        String base = getMetaProperty(Property.LIBRARY_VERSION);
        String branch = getMetaProperty(Property.VCS_BRANCH);
        if ("master".equals(branch)) return base;
        String sha = getMetaProperty(Property.VCS_COMMIT);
        return base + "-" + sha.substring(0, 7);
    }

    //

    private JTomlVersionInfo() { }

    //

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD })
    @MagicConstant(valuesFromClass = Property.class)
    private @interface Property {
        String LIBRARY_VERSION = "library.version";
        String VCS_BRANCH = "vcs.branch";
        String VCS_COMMIT = "vcs.commit";
    }

}
