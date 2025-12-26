package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@ApiStatus.Internal
final class TypeAdaptersImpl extends AbstractCollection<TypeAdapter<?>> implements TypeAdapters {

    static final TypeAdaptersImpl STANDARD;
    static {
        Builder builder = new Builder();
        TypeAdapter<?> next;
        for (Field field : TypeAdapter.class.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod)) continue;
            if (!Modifier.isPublic(mod)) continue;
            if (!TypeAdapter.class.isAssignableFrom(field.getType())) continue;
            try {
                next = (TypeAdapter<?>) field.get(null);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read value of constant " + field.getName());
            }
            builder.add(next);
        }
        STANDARD = builder.build();
    }

    //

    private final Map<Class<?>, TypeAdapter<?>> map;

    private TypeAdaptersImpl(@NotNull Map<Class<?>, TypeAdapter<?>> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    //

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public @NotNull Iterator<TypeAdapter<?>> iterator() {
        return this.map.values().iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> TypeAdapter<T> get(@NotNull Class<T> type) {
        TypeAdapter<?> raw = this.map.get(type);
        if (raw == null) return null;
        return (TypeAdapter<T>) raw;
    }

    //

    static final class Builder implements TypeAdapters.Builder {

        private final Map<Class<?>, TypeAdapter<?>> map;
        private volatile boolean open;

        Builder() {
            this.map = new HashMap<>();
            this.open = true;
        }

        //

        @Override
        public @NotNull Builder clear() {
            this.checkOpen();
            this.map.clear();
            return this;
        }

        @Override
        public @NotNull Builder add(@NotNull TypeAdapter<?> adapter) {
            this.checkOpen();

            Class<?> type = adapter.typeClass();
            this.map.put(type, adapter);

            type = getBoxedCounterpart(type);
            if (type != null) this.map.put(type, adapter);

            return this;
        }

        @Override
        public @NotNull TypeAdaptersImpl build() {
            this.checkOpen();
            this.open = false;
            return new TypeAdaptersImpl(this.map);
        }

        //

        private void checkOpen() throws IllegalStateException {
            if (this.open) return;
            throw new IllegalStateException("Cannot use builder after #build()");
        }

        private static @Nullable Class<?> getBoxedCounterpart(@NotNull Class<?> type) {
            if (!type.isPrimitive()) return null;
            String name = type.getName();
            switch (name.charAt(0)) {
                case 'b':
                    return name.charAt(1) == 'y' ? Byte.class : Boolean.class;
                case 'i':
                    return Integer.class;
                case 'f':
                    return Float.class;
                case 'd':
                    return Double.class;
                case 'c':
                    return Character.class;
                case 'l':
                    return Long.class;
                case 's':
                    return Short.class;
                default:
                    return Void.class;
            }
        }

    }

}
