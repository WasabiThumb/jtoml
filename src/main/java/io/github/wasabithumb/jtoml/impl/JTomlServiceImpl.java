package io.github.wasabithumb.jtoml.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.JTomlService;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class JTomlServiceImpl extends JTomlService {

    private static final JTomlImpl WITH_DEFAULTS = new JTomlImpl(JTomlOptions.defaults());

    public JTomlServiceImpl() { }

    @Override
    public @NotNull JToml defaultInstance() {
        return WITH_DEFAULTS;
    }

    @Override
    public @NotNull JToml createInstance(@NotNull JTomlOptions options) {
        return new JTomlImpl(options);
    }

}
