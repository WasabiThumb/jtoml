package io.github.wasabithumb.jtoml.util;

import java.util.NoSuchElementException;

/**
 * Expanding structure which holds booleans
 */
public final class BooleanBuffer {

    private long[] data;
    private int capacity;
    private int length;
    private int head;

    //

    public BooleanBuffer() {
        this.data = new long[1];
        this.capacity = 64;
        this.length = 0;
        this.head = 0;
    }

    //

    public int size() {
        return this.length - this.head;
    }

    public boolean isNotEmpty() {
        return this.head < this.length;
    }

    public void push(boolean b) {
        if (this.length == this.capacity) {
            int nc = this.capacity << 1;
            int nl = (nc >> 6);
            long[] cpy = new long[nl];
            System.arraycopy(this.data, 0, cpy, 0, this.data.length);
            this.data = cpy;
            this.capacity = nc;
        }

        long f = 1L << ((long) (this.length % 64));
        int idx = this.length / 64;
        if (b) {
            this.data[idx] |= f;
        } else {
            this.data[idx] &= (~f);
        }
        this.length++;
    }

    public boolean pop() throws NoSuchElementException {
        if (this.head >= this.length) throw new NoSuchElementException();
        int h = this.head++;

        long f = 1L << ((long) (h % 64));
        int idx = h / 64;

        return (this.data[idx] & f) != 0L;
    }

}
