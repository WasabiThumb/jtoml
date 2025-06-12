package io.github.wasabithumb.jtoml.comment;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

@ApiStatus.Internal
final class CommentsImpl implements Comments {

    private Comment[] array;
    private int capacity;
    private int preHead;
    private int postHead;
    private boolean hasInline;

    CommentsImpl() {
        this.clear();
    }

    //

    @Override
    public int count() {
        return this.postHead;
    }

    @Override
    public @NotNull @Unmodifiable List<Comment> all() {
        return Collections.unmodifiableList(Arrays.asList(this.array).subList(0, this.postHead));
    }

    @Override
    public @NotNull @Unmodifiable List<Comment> get(@NotNull CommentPosition position) {
        switch (position) {
            case PRE:
                return Collections.unmodifiableList(Arrays.asList(this.array).subList(0, this.preHead));
            case INLINE:
                if (!this.hasInline) return Collections.emptyList();
                return Collections.singletonList(this.array[this.preHead]);
            case POST:
                int start = this.preHead;
                if (this.hasInline) start++;
                return Collections.unmodifiableList(Arrays.asList(this.array).subList(start, this.postHead));
        }
        throw new AssertionError("Unreachable code");
    }

    @Override
    public void clear() {
        this.array = new Comment[0];
        this.capacity = 0;
        this.preHead = 0;
        this.postHead = 0;
        this.hasInline = false;
    }

    @Override
    public void clear(@NotNull CommentPosition position) {
        int tmp;
        switch (position) {
            case PRE:
                tmp = this.preHead;
                System.arraycopy(
                        this.array, tmp,
                        this.array, 0,
                        this.postHead - tmp
                );
                this.preHead = 0;
                this.postHead -= tmp;
                break;
            case INLINE:
                if (!this.hasInline) return;
                tmp = this.preHead + 1;
                System.arraycopy(
                        this.array, tmp,
                        this.array, this.preHead,
                        this.postHead - tmp
                );
                this.hasInline = false;
                this.postHead--;
                break;
            case POST:
                tmp = this.preHead;
                if (this.hasInline) tmp++;
                this.postHead = tmp;
                break;
        }
        this.tryShrink();
    }

    @Override
    public void add(@NotNull Comment comment) {
        switch (comment.position()) {
            case PRE:
                this.ensureCapacity();
                System.arraycopy(
                        this.array, this.preHead,
                        this.array, this.preHead + 1,
                        this.postHead - this.preHead
                );
                this.array[this.preHead++] = comment;
                this.postHead++;
                break;
            case INLINE:
                if (!this.hasInline) {
                    this.ensureCapacity();
                    System.arraycopy(
                            this.array, this.preHead,
                            this.array, this.preHead + 1,
                            this.postHead - this.preHead
                    );
                    this.hasInline = true;
                    this.postHead++;
                }
                this.array[this.preHead] = comment;
                break;
            case POST:
                this.ensureCapacity();
                this.array[this.postHead++] = comment;
                break;
        }
    }

    private void ensureCapacity() {
        int capacity = this.capacity;
        if (capacity == 0) {
            this.array = new Comment[1];
            this.capacity = 1;
        } else {
            int required = this.postHead + 1;
            if (required <= capacity) return;
            do {
                capacity <<= 1;
            } while (required > capacity);
            Comment[] cpy = new Comment[capacity];
            System.arraycopy(this.array, 0, cpy, 0, this.postHead);
            this.array = cpy;
            this.capacity = capacity;
        }
    }

    private void tryShrink() {
        int tc = this.capacity;
        int cc;
        boolean any = false;

        while (true) {
            cc = (tc >> 1);
            if (cc >= this.postHead) {
                tc = cc;
                any = true;
            } else {
                break;
            }
        }
        if (!any) return;

        Comment[] shrink = new Comment[tc];
        System.arraycopy(this.array, 0, shrink, 0, this.postHead);
        this.array = shrink;
        this.capacity = tc;
    }

    @Override
    public int hashCode() {
        int h = 7;
        for (int i=0; i < this.postHead; i++) {
            h = 31 * h + Objects.hashCode(this.array[i]);
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommentsImpl)) return false;
        CommentsImpl other = (CommentsImpl) obj;
        if (this.postHead != other.postHead) return false;
        for (int i=0; i < this.postHead; i++) {
            if (!Objects.equals(this.array[i], other.array[i])) return false;
        }
        return true;
    }

    @Override
    public @NotNull String toString() {
        StringJoiner sj = new StringJoiner(", ");
        for (int i=0; i < this.postHead; i++) sj.add(this.array[i].toString());
        return "Comments[" + sj + "]";
    }

}
