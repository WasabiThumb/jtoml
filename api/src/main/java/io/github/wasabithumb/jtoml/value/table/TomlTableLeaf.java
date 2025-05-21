package io.github.wasabithumb.jtoml.value.table;

import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class TomlTableLeaf implements TomlTableNode {

    private final TomlValue value;

    TomlTableLeaf(@NotNull TomlValue value) {
        this.value = value;
    }

    //

    public @NotNull TomlValue value() {
        return this.value;
    }

    // START Node Super


    @Override
    public int entryCount() {
        return 1;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    @Contract("-> fail")
    public @NotNull TomlTableBranch asBranch() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    @Contract("-> this")
    public @NotNull TomlTableLeaf asLeaf() {
        return this;
    }

    // END Node Super

}
