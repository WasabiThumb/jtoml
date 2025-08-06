package io.github.wasabithumb.jtoml.dummy;

import io.github.wasabithumb.jtoml.serial.TomlSerializable;

import java.util.Objects;

public final class Named implements TomlSerializable {

    private final String name;

    private Named() {
        this.name = null;
    }

    public Named(String name) {
        this.name = name;
    }

    //

    public String name() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Named other)) return false;
        return Objects.equals(this.name, other.name);
    }

}
