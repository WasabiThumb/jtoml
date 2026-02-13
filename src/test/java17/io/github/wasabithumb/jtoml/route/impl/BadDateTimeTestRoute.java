package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.except.TomlValueException;
import io.github.wasabithumb.jtoml.route.TestRoute;

import java.time.LocalDate;

public final class BadDateTimeTestRoute implements TestRoute.Failing<TomlValueException> {

    @Override
    public String displayName() {
        return "Bad Date-Time";
    }

    @Override
    public Class<TomlValueException> errorType() {
        return TomlValueException.class;
    }

    @Override
    public void executeFailing(JToml instance) throws TomlValueException {
        DateTable table = new DateTable(
                LocalDate.now(),
                LocalDate.of(-1, 7, 16) // year -1
        );
        instance.toToml(table);
    }

    //

    private record DateTable(
            LocalDate valid,
            LocalDate invalid
    ) { }

}
