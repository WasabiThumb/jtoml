package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.helper.BirthdaysTestRoute;

import java.util.function.IntFunction;

public final class ArrayBirthdaysTestRoute extends BirthdaysTestRoute {

    @Sentinel("birthdays.toml")
    private Document document;

    //

    @Override
    public String displayName() {
        return "Birthdays to Array";
    }

    @Override
    public void execute(JToml instance) {
        final Person[] people = this.document.people;
        this.validatePeople(people.length, new ArrayAccess<>(people));
    }

    //

    private record Document(
            Person[] people
    ) { }

    private record ArrayAccess<T>(
            T[] array
    ) implements IntFunction<T> {
        @Override
        public T apply(int i) {
            return this.array[i];
        }
    }

}
