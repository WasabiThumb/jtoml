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

package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.document.TomlIssue;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jspecify.annotations.NullMarked;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@NullMarked
public final class ErrorRecoveryTestRoute implements TestRoute.Configuring {

    @Sentinel("error-recovery.toml")
    private TomlDocument document;

    //

    @Override
    public String displayName() {
        return "Error Recovery";
    }

    @Override
    public void execute(JToml instance) {
        List<TomlIssue> issues = this.document.issues();
        assertEquals(3, issues.size());
        assertEquals(TomlPrimitive.of(42), this.document.get("good.x"));
        assertEquals(TomlPrimitive.of("wonderful"), this.document.get("good.y"));
    }

    @Override
    public void configure(JTomlOptions.Builder options) {
        options.set(JTomlOption.ERROR_RECOVERY, true);
    }

}
