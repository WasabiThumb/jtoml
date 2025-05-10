package io.github.wasabithumb.jtoml.test;

import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.io.IOException;
import java.io.InputStream;

public interface TestSpec {

    static TestSpec valid(String name, boolean hasValidator) {
        return new ValidTestSpec(name, hasValidator);
    }

    static TestSpec invalid(String name) {
        return new InvalidTestSpec(name);
    }

    //

    /**
     * Name of the test
     */
    String name();

    /**
     * Reads the TOML document for this test
     */
    InputStream read() throws IOException;

    /**
     * Returns true if the parsing
     * should fail
     */
    boolean shouldFail();

    /**
     * Checks if a successfully parsed table
     * has the correct content
     */
    void validate(TomlTable table) throws IOException;

}
