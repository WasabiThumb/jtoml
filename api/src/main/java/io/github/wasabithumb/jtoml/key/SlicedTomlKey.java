package io.github.wasabithumb.jtoml.key;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@ApiStatus.Internal
final class SlicedTomlKey extends AbstractTomlKey {

    public static @NotNull SlicedTomlKey of(
            @NotNull TomlKey source,
            int offset,
            int length
    ) throws IllegalArgumentException {
        final int sl = source.size();
        if (offset < 0) throw new IllegalArgumentException("Offset may not be negative (got " + offset + ")");
        if (length < 0) throw new IllegalArgumentException("Length may not be negative (got " + length + ")");
        if ((offset + length) > sl) {
            throw new IllegalArgumentException("Illegal offset and length (" + offset + ", " +
                    length + ") for size " + sl);
        }
        if (source instanceof SlicedTomlKey) {
            SlicedTomlKey qual = (SlicedTomlKey) source;
            return new SlicedTomlKey(qual.source, qual.offset + offset, length);
        } else {
            return new SlicedTomlKey(source, offset, length);
        }
    }

    //

    private final TomlKey source;
    private final int offset;
    private final int length;

    private SlicedTomlKey(@NotNull TomlKey source, int offset, int length) {
        this.source = source;
        this.offset = offset;
        this.length = length;
    }

    //

    @Override
    public int size() {
        return this.length;
    }

    @Override
    public @NotNull String get(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= this.length)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + this.length);
        return this.source.get(this.offset + index);
    }

    @Override
    public @NotNull Stream<String> stream() {
        return this.source.stream()
                .skip(this.offset)
                .limit(this.length);
    }

}
