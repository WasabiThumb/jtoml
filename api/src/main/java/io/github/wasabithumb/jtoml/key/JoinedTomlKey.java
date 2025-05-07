package io.github.wasabithumb.jtoml.key;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@ApiStatus.Internal
final class JoinedTomlKey extends AbstractTomlKey {

    public static @NotNull TomlKey join(@NotNull TomlKey first, @NotNull TomlKey @NotNull ... additional) {
        if (additional.length == 0) return first;
        TomlKey[] parts;
        int offset;
        int totalSize;

        if (first instanceof JoinedTomlKey) {
            JoinedTomlKey qual = (JoinedTomlKey) first;
            TomlKey[] qualSub = qual.sub;
            offset = qualSub.length;
            totalSize = qual.totalSize;
            parts = new TomlKey[additional.length + offset];
            System.arraycopy(qualSub, 0, parts, 0, offset);
        } else if (first.isEmpty()) {
            parts = new TomlKey[additional.length];
            offset = 0;
            totalSize = 0;
        } else {
            parts = new TomlKey[additional.length + 1];
            offset = 1;
            totalSize = first.size();
            parts[0] = first;
        }

        TomlKey next;
        for (int i=0; i < additional.length; i++) {
            next = additional[i];
            parts[offset + i] = next;
            totalSize += next.size();
        }
        return new JoinedTomlKey(parts, totalSize);
    }

    //

    private final TomlKey[] sub;
    private final int totalSize;

    private JoinedTomlKey(@NotNull TomlKey @NotNull [] sub, int totalSize) {
        this.sub = sub;
        this.totalSize = totalSize;
    }

    //

    @Override
    public int size() {
        return this.totalSize;
    }

    @Override
    public @NotNull Stream<String> stream() {
        Stream<String> ret = this.sub[0].stream();
        for (int i=1; i < this.sub.length; i++) {
            ret = Stream.concat(ret, this.sub[i].stream());
        }
        return ret;
    }

}
