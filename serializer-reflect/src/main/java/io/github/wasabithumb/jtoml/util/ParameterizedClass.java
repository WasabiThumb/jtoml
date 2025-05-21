package io.github.wasabithumb.jtoml.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Wraps {@link Class} and {@link Type}
 * (as provided by e.g. {@link Field#getType()} and {@link Field#getGenericType()})
 * and provides utilities to work with them
 */
@ApiStatus.Internal
public final class ParameterizedClass<T> {

    @Contract("_ -> new")
    public static @NotNull ParameterizedClass<?> of(@NotNull Field f) {
        return new ParameterizedClass<>(f.getType(), f.getGenericType());
    }

    @Contract("_ -> new")
    public static @NotNull ParameterizedClass<?> of(@NotNull Type t) {
        Type[] params;
        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            params = pt.getActualTypeArguments();
            t = pt.getRawType();
        } else {
            params = new Type[0];
        }

        Class<?> cls;
        if (t instanceof Class<?>) {
            cls = (Class<?>) t;
        } else {
            cls = Object.class;
        }
        return new ParameterizedClass<>(cls, params);
    }

    //

    private final Class<T> clazz;
    private final Type[] params;

    private ParameterizedClass(@NotNull Class<T> clazz, @NotNull Type[] params) {
        this.clazz = clazz;
        this.params = params;
    }

    private ParameterizedClass(@NotNull Class<T> clazz, @NotNull Type type, @NotNull Type @Nullable [] inherited) {
        Type[] params;
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            params = pt.getActualTypeArguments();

            Type raw = pt.getRawType();
            if (!clazz.equals(raw)) {
                throw new IllegalArgumentException("Raw type (" + raw + ") is not of stated class " + clazz.getName());
            }

            if (inherited != null)
                System.arraycopy(inherited, 0, params, 0, Math.min(inherited.length, params.length));
        } else {
            params = new Type[0];
        }

        this.clazz = clazz;
        this.params = params;
    }

    public ParameterizedClass(@NotNull Class<T> clazz, @NotNull Type type) {
        this(clazz, type, null);
    }

    public ParameterizedClass(@NotNull Class<T> clazz) {
        this(clazz, new Type[0]);
    }

    //

    @Contract(pure = true)
    public @NotNull Class<T> raw() {
        return this.clazz;
    }

    public int paramCount() {
        return this.params.length;
    }

    @Contract(pure = true)
    public @NotNull Type @NotNull [] params() {
        return Arrays.copyOf(this.params, this.params.length);
    }

    public @NotNull Type param(int index) {
        return this.params[index];
    }

    public @Nullable ParameterizedClass<?> superClass() {
        Class<?> cls = this.clazz.getSuperclass();
        if (cls == null) return null;
        return new ParameterizedClass<>(cls, this.clazz.getGenericSuperclass(), this.params);
    }

    public @NotNull Set<ParameterizedClass<?>> superClasses() {
        Set<ParameterizedClass<?>> ret = new LinkedHashSet<>();
        Iterator<ParameterizedClass<?>> iter = this.lineage(false);
        while (iter.hasNext()) ret.add(iter.next());
        return Collections.unmodifiableSet(ret);
    }

    public @NotNull Set<ParameterizedClass<?>> directSuperInterfaces() {
        Class<?>[] a = this.clazz.getInterfaces();
        Type[] b = this.clazz.getGenericInterfaces();
        int count = a.length;
        assert count == b.length;

        Set<ParameterizedClass<?>> ret = new HashSet<>(count);
        for (int i=0; i < count; i++)
            ret.add(new ParameterizedClass<>(a[i], b[i], this.params));
        return Collections.unmodifiableSet(ret);
    }

    public @NotNull Set<ParameterizedClass<?>> superInterfaces() {
        Set<ParameterizedClass<?>> ret = new HashSet<>();
        Iterator<ParameterizedClass<?>> iter = this.lineage(true);
        while (iter.hasNext()) ret.addAll(iter.next().directSuperInterfaces());
        return Collections.unmodifiableSet(ret);
    }

    public @Nullable ParameterizedClass<?> declaredInterface(@NotNull Class<?> cls) {
        if (this.clazz.isInterface() && this.clazz.equals(cls)) return this;
        for (ParameterizedClass<?> pc : this.superInterfaces()) {
            if (cls.equals(pc.raw())) return pc;
        }
        return null;
    }

    private @NotNull Iterator<ParameterizedClass<?>> lineage(final boolean includeSelf) {
        return new Iterator<ParameterizedClass<?>>() {

            private ParameterizedClass<?> head = includeSelf ?
                    ParameterizedClass.this :
                    ParameterizedClass.this.superClass();

            @Override
            public boolean hasNext() {
                return this.head != null;
            }

            @Override
            public ParameterizedClass<?> next() {
                ParameterizedClass<?> ret = this.head;
                if (ret == null) throw new NoSuchElementException();
                this.head = ret.superClass();
                return ret;
            }

        };
    }

    //

    @Override
    public @NotNull String toString() {
        if (this.params.length == 0)
            return this.clazz.getName();

        StringBuilder sb = new StringBuilder(this.clazz.getName());
        sb.append('<');

        for (int i=0; i < this.params.length; i++) {
            if (i != 0) sb.append(", ");
            Type t = this.params[i];
            if (t instanceof Class<?>) {
                Class<?> tc = (Class<?>) t;
                int na = 0;
                while (tc.isArray()) {
                    na++;
                    tc = tc.getComponentType();
                }
                sb.append(tc.getName());
                for (int z=0; z < na; z++) sb.append("[]");
            } else {
                sb.append(t.getTypeName());
            }
        }

        return sb.append('>').toString();
    }

    @Override
    public int hashCode() {
        int h = 7;
        h = 31 * h + this.clazz.hashCode();
        for (Type param : this.params)
            h = 31 * h + param.hashCode();
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterizedClass<?>)) return false;
        ParameterizedClass<?> other = (ParameterizedClass<?>) obj;
        if (!this.clazz.equals(other.clazz)) return false;
        return Arrays.equals(this.params, other.params);
    }

}
