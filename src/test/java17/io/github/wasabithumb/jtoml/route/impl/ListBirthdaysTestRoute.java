package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.helper.BirthdaysTestRoute;

import java.util.List;

public final class ListBirthdaysTestRoute extends BirthdaysTestRoute {

    @Sentinel("birthdays.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Birthdays to List";
    }

    @Override
    public void execute(JToml instance) {
        List<Person> people = this.document.people;
        this.validatePeople(people.size(), people::get);
    }

    //

    private record Document(
            List<Person> people
    ) { }

}
