package io.github.wasabithumb.jtoml.test;

import io.github.wasabithumb.jtoml.value.table.TomlTable;

final class InvalidTestSpec extends AbstractTestSpec {

    public InvalidTestSpec(String name) {
        super(name);
    }

    //

    @Override
    public boolean shouldFail() {
        return true;
    }

    @Override
    public void validate(TomlTable table) {
        throw new UnsupportedOperationException();
    }

}
