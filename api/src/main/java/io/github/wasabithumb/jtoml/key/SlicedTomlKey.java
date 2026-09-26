/*
 * Copyright 2026 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wasabithumb.jtoml.key;

import org.jetbrains.annotations.ApiStatus;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

@ApiStatus.Internal
class SlicedTomlKey extends AbstractTomlKey {

    public static SlicedTomlKey of(
            TomlKey source,
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
            return qual.source instanceof RandomAccess ?
                    new SlicedTomlKey.WithRandomAccess(qual.source, qual.offset + offset, length) :
                    new SlicedTomlKey(qual.source, qual.offset + offset, length);
        } else {
            return source instanceof RandomAccess ?
                    new SlicedTomlKey.WithRandomAccess(source, offset, length) :
                    new SlicedTomlKey(source, offset, length);
        }
    }

    //

    private final TomlKey source;
    private final int offset;
    private final int length;

    private SlicedTomlKey(TomlKey source, int offset, int length) {
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
    public String get(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= this.length)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + this.length);
        return this.source.get(this.offset + index);
    }

    @Override
    public Iterator<String> iterator() {
        return this.listIterator(0);
    }

    @Override
    public ListIterator<String> listIterator(int index) {
        this.iteratorIndexCheck(index);
        return new SequentialIter(this, index);
    }

    protected void iteratorIndexCheck(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index > this.length)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + this.length);
    }

    //

    private static final class WithRandomAccess
            extends SlicedTomlKey
            implements RandomAccess
    {

        WithRandomAccess(TomlKey source, int offset, int length) {
            super(source, offset, length);
        }

        @Override
        public ListIterator<String> listIterator(int index) {
            this.iteratorIndexCheck(index);
            return new ArrayIter(this, index);
        }

    }

    private static final class ArrayIter implements ListIterator<String> {

        private final SlicedTomlKey parent;
        private int head;

        ArrayIter(SlicedTomlKey parent, int head) {
            this.parent = parent;
            this.head = head;
        }

        //

        @Override
        public boolean hasNext() {
            return this.head < this.parent.length;
        }

        @Override
        public String next() {
            int index = this.head;
            if (index >= this.parent.length) throw new NoSuchElementException();
            String ret = this.parent.source.get(this.parent.offset + index);
            this.head = index + 1;
            return ret;
        }

        @Override
        public boolean hasPrevious() {
            return this.head != 0;
        }

        @Override
        public String previous() {
            int index = this.head;
            if (index == 0) throw new NoSuchElementException();
            index--;
            String ret = this.parent.source.get(this.parent.offset + index);
            this.head = index;
            return ret;
        }

        @Override
        public int nextIndex() {
            return this.head;
        }

        @Override
        public int previousIndex() {
            return this.head - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(String s) {
            throw new UnsupportedOperationException();
        }

    }

    private static final class SequentialIter implements ListIterator<String> {

        private final SlicedTomlKey parent;
        private final ListIterator<String> backing;

        SequentialIter(SlicedTomlKey parent, int start) {
            this.parent = parent;
            this.backing = this.parent.source.listIterator(parent.offset + start);
        }

        //

        @Override
        public boolean hasNext() {
            return (this.backing.nextIndex() - this.parent.offset) < this.parent.length;
        }

        @Override
        public String next() {
            if ((this.backing.nextIndex() - this.parent.offset) >= this.parent.length) throw new NoSuchElementException();
            return this.backing.next();
        }

        @Override
        public boolean hasPrevious() {
            return this.backing.previousIndex() >= this.parent.offset;
        }

        @Override
        public String previous() {
            if (this.backing.previousIndex() < this.parent.offset) throw new NoSuchElementException();
            return this.backing.previous();
        }

        @Override
        public int nextIndex() {
            int next = this.backing.nextIndex() - this.parent.offset;
            return Math.min(next, this.parent.length);
        }

        @Override
        public int previousIndex() {
            int pre = this.backing.previousIndex();
            if (pre < this.parent.offset) return -1;
            return pre - this.parent.offset;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(String s) {
            throw new UnsupportedOperationException();
        }

    }

}
