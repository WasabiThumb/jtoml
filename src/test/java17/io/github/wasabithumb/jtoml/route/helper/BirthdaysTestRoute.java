package io.github.wasabithumb.jtoml.route.helper;

import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;

import java.time.LocalDate;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BirthdaysTestRoute implements TestRoute {

    private static final int PEOPLE_COUNT = 8;

    //

    /** Match an ordered collection to the content of birthdays.toml */
    protected void validatePeople(int count, IntFunction<Person> getter) {
        assertEquals(PEOPLE_COUNT, count);
        Person previous = getter.apply(0);
        for (int i = 1; i < PEOPLE_COUNT; i++) {
            Person next = getter.apply(i);
            assertFalse(
                    next.dateOfBirth.isBefore(previous.dateOfBirth),
                    "not in ascending date order"
            );
            previous = next;
        }
    }

    //

    @Convention.Kebab
    protected record Person(
            String name,
            LocalDate dateOfBirth
    ) { }

}
