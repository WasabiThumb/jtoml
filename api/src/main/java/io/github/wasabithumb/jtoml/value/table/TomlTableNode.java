package io.github.wasabithumb.jtoml.value.table;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
interface TomlTableNode {

    int entryCount();

    boolean isBranch();

    @NotNull TomlTableBranch asBranch() throws UnsupportedOperationException;

    boolean isLeaf();

    @NotNull TomlTableLeaf asLeaf() throws UnsupportedOperationException;

}
