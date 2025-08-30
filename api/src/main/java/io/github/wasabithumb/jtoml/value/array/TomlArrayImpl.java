package io.github.wasabithumb.jtoml.value.array;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.value.TomlValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@ApiStatus.Internal
final class TomlArrayImpl implements TomlArray {

    static @NotNull TomlArrayImpl copyOf(@NotNull Iterable<? extends TomlValue> src) {
        TomlArrayImpl ret;

        if (src instanceof TomlArray) {
            TomlArrayImpl other = (TomlArrayImpl) src;
            ret = new TomlArrayImpl(other.backing.size(), Comments.copyOf(other.comments));
        } else if (src instanceof Collection<?>) {
            ret = new TomlArrayImpl(((Collection<?>) src).size());
        } else {
            ret = new TomlArrayImpl();
        }

        for (TomlValue tv : src) {
            ret.backing.add(TomlValue.copyOf(tv));
        }

        return ret;
    }

    //

    private final List<TomlValue> backing;
    private final Comments comments;
    private transient byte flags;

    private TomlArrayImpl(int initialCapacity, @NotNull Comments comments) {
        this.backing = new ArrayList<>(initialCapacity);
        this.comments = comments;
        this.flags = 0;
    }

    TomlArrayImpl(int initialCapacity) {
        this(initialCapacity, Comments.empty());
    }

    TomlArrayImpl() {
        this(10, Comments.empty());
    }

    //


    @Override
    public int flags() {
        return this.flags & 0xFF;
    }

    @Override
    public @NotNull TomlArray flags(int flags) {
        this.flags = (byte) flags;
        return this;
    }

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
