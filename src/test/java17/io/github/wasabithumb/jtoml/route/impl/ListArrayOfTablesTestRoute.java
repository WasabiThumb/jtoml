package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;

import java.time.LocalDate;
import java.util.List;

public final class ListArrayOfTablesTestRoute implements TestRoute {

    @Sentinel("array-of-tables.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Array of Tables to List";
    }

    @Override
    public void execute(JToml instance) {
        // TODO
        System.out.println(this.document);
    }

    //

    private record Document(
            List<Person> people
    ) { }

    @Convention.Kebab
    private record Person(
            String name,
            LocalDate dateOfBirth
    ) { }

}
