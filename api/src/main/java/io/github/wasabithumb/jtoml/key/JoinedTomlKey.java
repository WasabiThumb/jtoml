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

@ApiStatus.Internal
final class JoinedTomlKey extends AbstractTomlKey {

    public static TomlKey join(TomlKey first, TomlKey ... additional) {
        if (additional.length == 0) return first;
        TomlKey[] parts;
        int head;
        int totalSize;

        if (first instanceof JoinedTomlKey) {
            JoinedTomlKey qual = (JoinedTomlKey) first;
            TomlKey[] qualSub = qual.sub;
            head = qualSub.length;
            totalSize = qual.totalSize;
            parts = new TomlKey[additional.length + head];
            System.arraycopy(qualSub, 0, parts, 0, head);
        } else if (first.isEmpty()) {
            parts = new TomlKey[additional.length];
            head = 0;
            totalSize = 0;
        } else {
            parts = new TomlKey[additional.length + 1];
            head = 1;
            totalSize = first.size();
            parts[0] = first;
        }

        for (TomlKey next : additional) {
            if (next.isEmpty()) continue;
            if (!(next instanceof JoinedTomlKey)) {
                parts[head++] = next;
                totalSize += next.size();
                continue;
            }
            JoinedTomlKey qual = (JoinedTomlKey) next;
            int qn = qual.sub.length;
            if (qn == 0) continue;
            TomlKey[] cpy = new TomlKey[parts.length + qn - 1];
            System.arraycopy(parts, 0, cpy, 0, head);
            System.arraycopy(qual.sub, 0, cpy, head, qn);
            parts = cpy;
            head += qn;
            totalSize += qual.totalSize;
        }

        if (head == 1) return parts[0];
        if (head != parts.length) {
            TomlKey[] cpy = new TomlKey[head];
            System.arraycopy(parts, 0, cpy, 0, head);
            parts = cpy;
        }

        return new JoinedTomlKey(parts, totalSize);
    }

    //

    /** @implNote No sub-key may have a length of 0 */
    private final TomlKey[] sub;
    private final int totalSize;

    private JoinedTomlKey(TomlKey[] sub, int totalSize) {
        this.sub = sub;
        this.totalSize = totalSize;
    }

    //

    @Override
    public int size() {
        return this.totalSize;
    }

    @Override
    public String get(int index) {
        if (index < 0 || index >= this.totalSize)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + this.totalSize);

        int head = 0;
        while (head < this.sub.length) {
            TomlKey k = this.sub[head++];
            int ks = k.size();
            if (index < ks) return k.get(index);
            index -= ks;
        }

        throw new IllegalStateException("Size invariant violated");
    }

    @Override
    public Iterator<String> iterator() {
        return this.listIterator(0);
    }

    @Override
    public ListIterator<String> listIterator(int index) {
        if (index < 0 || index > this.totalSize)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + this.totalSize);
        return new Iter(this, index);
    }

    //

    private static final class Iter implements ListIterator<String> {

        private final JoinedTomlKey parent;
        private int keyHead;
        private int partOffset;
        private int partHead;

        Iter(JoinedTomlKey parent, int start) {
            int keyHead = 0;
            int partOffset = 0;
            while (keyHead < parent.sub.length) {
                TomlKey k = parent.sub[keyHead];
                int ks = k.size();
                if (start < ks) break;
                keyHead++;
                partOffset += ks;
                start -= ks;
            }
            this.parent = parent;
            this.keyHead = keyHead;
            this.partOffset = partOffset;
            this.partHead = 0;
        }

        //

        @Override
        public boolean hasNext() {
            if (this.keyHead >= this.parent.sub.length) return false;
            return this.partHead < this.parent.sub[this.keyHead].size();
        }

        @Override
        public String next() {
            int keyIndex = this.keyHead;
            if (keyIndex >= this.parent.sub.length) throw new NoSuchElementException();
            TomlKey key = this.parent.sub[keyIndex];
            int keySize = key.size();
            int partIndex = this.partHead;
            if (partIndex >= keySize) throw new NoSuchElementException();
            String ret = key.get(partIndex++);
            if (partIndex == keySize) {
                this.keyHead = keyIndex + 1;
                this.partOffset += keySize;
                this.partHead = 0;
            } else {
                this.partHead = partIndex;
            }
            return ret;
        }

        @Override
        public boolean hasPrevious() {
            return this.partHead != 0 ||
                    this.keyHead != 0;
        }

        @Override
        public String previous() {
            int keyIndex = this.keyHead;
            int partIndex = this.partHead;
            if (partIndex != 0) {
                TomlKey key = this.parent.sub[keyIndex];
                this.partHead = --partIndex;
                return key.get(partIndex);
            }
            if (keyIndex != 0) {
                TomlKey key = this.parent.sub[--keyIndex];
                int ks = key.size();
                this.keyHead = keyIndex;
                this.partHead = partIndex = ks - 1;
                this.partOffset -= ks;
                return key.get(partIndex);
            }
            throw new NoSuchElementException();
        }

        @Override
        public int nextIndex() {
            return this.partOffset + this.partHead;
        }

        @Override
        public int previousIndex() {
            return this.partOffset + this.partHead - 1;
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
