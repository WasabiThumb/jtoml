package io.github.wasabithumb.jtoml.value.array;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ApiStatus.Internal
final class TomlArrayImpl implements TomlArray {

    @Contract("_ -> new")
    static @NotNull TomlArrayImpl copyOf(@NotNull TomlArrayImpl other) {
        TomlArrayImpl ret = new TomlArrayImpl(other.size());
        ret.backing.addAll(other.backing);
        return ret;
    }

    //

    private final List<TomlValue> backing;
    private final Comments comments;

    TomlArrayImpl(int initialCapacity) {
        this.backing = new ArrayList<>(initialCapacity);
        this.comments = Comments.empty();
    }

    TomlArrayImpl() {
        this.backing = new ArrayList<>();
        this.comments = Comments.empty();
    }

    //

    @Override
    public @NotNull Comments comments() {
        return this.comments;
    }

    @Override
    public int size() {
        return this.backing.size();
    }

    @Override
    public @NotNull TomlValue get(int index) throws IndexOutOfBoundsException {
        return this.backing.get(index);
    }

    @Override
    public void add(TomlValue value) {
        if (value == null) throw new NullPointerException("Cannot add null to TomlArray");
        this.backing.add(value);
    }

    @Override
    public @NotNull TomlValue remove(int index) throws IndexOutOfBoundsException {
        return this.backing.remove(index);
    }

    @Override
    public @NotNull TomlValue set(int index, TomlValue value) throws IndexOutOfBoundsException {
        if (value == null) throw new NullPointerException("Cannot insert null into TomlArray");
        return this.backing.set(index, value);
    }

    @Override
    public @NotNull Iterator<TomlValue> iterator() {
        return this.backing.iterator();
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i=0; i < this.size(); i++) {
            if (i != 0) sb.append(", ");
            sb.append(this.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
