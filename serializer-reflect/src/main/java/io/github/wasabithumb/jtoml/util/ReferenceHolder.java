/*
 * Copyright 2025 Xavier Pedraza
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

package io.github.wasabithumb.jtoml.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Holds {@link java.lang.ref.WeakReference}s,
 * currently only for the purpose of checking for recursion
 * in the reflection deserializer.
 * This is distinct from a {@link java.util.WeakHashMap}'s key set
 * in that the equality function is as strict as possible within
 * JVM constraints. Two objects which are "equal" may not
 * be treated as equal by the ReferenceHolder.
 * This is entirely intended, as the strict equality of objects is
 * the only important property for handling recursion.
 */
@ApiStatus.Internal
public final class ReferenceHolder {

    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75d;

    private static final boolean SUPPORTS_REFERS_TO;
    private static final Method REFERS_TO;
    static {
        Method refersTo = null;
        boolean supportsRefersTo = false;
        try {
            //noinspection JavaReflectionMemberAccess
            refersTo = Reference.class.getMethod("refersTo", Object.class);
            supportsRefersTo = true;
        } catch (ReflectiveOperationException | SecurityException ignored) { }
        SUPPORTS_REFERS_TO = supportsRefersTo;
        REFERS_TO = refersTo;
    }

    private static boolean refEqual(@NotNull Object value, @NotNull WeakReference<Object> ref) {
        if (SUPPORTS_REFERS_TO) {
            try {
                return (Boolean) REFERS_TO.invoke(ref, value);
            } catch (ReflectiveOperationException | SecurityException e) {
                throw new AssertionError(e);
            }
        }
        return value.equals(ref.get());
    }

    @Contract("_ -> new")
    public static @NotNull ReferenceHolder copyOf(@NotNull ReferenceHolder other) {
        final int capacity = other.capacity;
        ReferenceHolder ret = new ReferenceHolder(capacity);
        System.arraycopy(other.buckets, 0, ret.buckets, 0, capacity);
        ret.size = other.size;
        return ret;
    }

    //

    private int capacity;
    private Bucket[] buckets;
    private int size;

    private ReferenceHolder(int capacity) {
        this.capacity = capacity;
        this.buckets = new Bucket[capacity];
        this.size = 0;
    }

    public ReferenceHolder() {
        this(INITIAL_CAPACITY);
    }

    //

    private void resize(int newCapacity) {
        Bucket[] nb = new Bucket[newCapacity];
        Bucket next;
        Object referent;
        int newSize = 0;

        for (int i=0; i < this.capacity; i++) {
            next = this.buckets[i];
            while (next != null) {
                referent = next.value.get();
                if (referent != null) {
                    int idx = this.hash(referent, newCapacity);
                    Bucket newBucket = new Bucket(next.value, nb[idx]);
                    nb[idx] = newBucket;
                    newSize++;
                }
                next = next.next;
            }
        }

        this.capacity = newCapacity;
        this.buckets = nb;
        this.size = newSize;
    }

    public boolean add(@NotNull Object object) {
        int hash = this.hash(object, this.capacity);
        final Bucket root = this.buckets[hash];
        Bucket next = root;
        while (next != null) {
            if (refEqual(object, next.value)) return false;
            next = next.next;
        }
        Bucket newBucket = new Bucket(new WeakReference<>(object), root);
        this.buckets[hash] = newBucket;
        this.size++;

        double load = ((double) this.size) / ((double) this.capacity);
        if (load > LOAD_FACTOR) this.resize(this.capacity << 1);
        return true;
    }

    public boolean contains(@NotNull Object object) {
        int hash = this.hash(object, this.capacity);
        Bucket next = this.buckets[hash];
        while (next != null) {
            if (refEqual(object, next.value)) return true;
            next = next.next;
        }
        return false;
    }

    private int hash(@NotNull Object object, int mod) {
        long h = Integer.toUnsignedLong(System.identityHashCode(object));
        h %= mod;
        return (int) h;
    }

    //

    private static final class Bucket {

        WeakReference<Object> value;
        Bucket next;

        Bucket(@NotNull WeakReference<Object> value, @Nullable Bucket next) {
            this.value = value;
            this.next = next;
        }

    }

}
