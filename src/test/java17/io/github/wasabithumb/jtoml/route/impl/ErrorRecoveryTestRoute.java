package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.document.TomlIssue;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
