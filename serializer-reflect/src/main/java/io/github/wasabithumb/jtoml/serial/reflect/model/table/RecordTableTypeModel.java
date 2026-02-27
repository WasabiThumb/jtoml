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

package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.convention.KeyConvention;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.recsup.RecordClass;
import io.github.wasabithumb.recsup.RecordComponent;
import io.github.wasabithumb.recsup.RecordSupport;
import org.jetbrains.annotations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@ApiStatus.Internal
final class RecordTableTypeModel<T> extends AbstractTableTypeModel<T> {

    private static @NotNull Key recordComponentKey(
            @NotNull RecordComponent component,
            @NotNull KeyConvention defaultConvention
    ) {
        return new RecordComponentKey(component, defaultConvention);
    }

    private static @NotNull RecordComponent unwrapRecordComponentKey(
            @NotNull Key key
    ) {
        if (key instanceof RecordComponentKey) {
            return ((RecordComponentKey) key).component;
        }
        throw new IllegalArgumentException("Key " + key + " is not a RecordComponentKey");
    }

    //

    private final RecordClass<T> clazz;
    private final RecordComponent[] components;

    RecordTableTypeModel(@NotNull Class<T> clazz) {
        this.clazz = RecordSupport.asRecord(clazz);
        this.components = this.clazz.getRecordComponents();
    }

    //

    @Override
    public @NotNull Class<T> type() {
        return this.clazz.handle();
    }

    @Override
    public @NotNull TableTypeModel.Builder<T> create() {
        return new Builder<>(this);
    }

    @Override
    public @NotNull Mapper mapper(@NotNull KeyConvention defaultConvention) {
        return new FixedMapper(this, this.keys(defaultConvention));
    }

    @Override
    public @NotNull @Unmodifiable Collection<Key> keys(@NotNull T instance, @NotNull KeyConvention defaultConvention) {
        return this.keys(defaultConvention);
    }

    private @NotNull @Unmodifiable Collection<Key> keys(@NotNull KeyConvention defaultConvention) {
        List<Key> ret = new ArrayList<>(this.components.length);
        for (RecordComponent rc : this.components) ret.add(recordComponentKey(rc, defaultConvention));
        return Collections.unmodifiableList(ret);
    }

    @Override
    public @NotNull ParameterizedClass<?> elementType(@NotNull Key key) {
        final RecordComponent componentKey = unwrapRecordComponentKey(key);
        return new ParameterizedClass<>(componentKey.getType(), componentKey.getGenericType());
    }

    @Override
    public @UnknownNullability Object get(@NotNull T instance, @NotNull Key key) {
        final RecordComponent component = unwrapRecordComponentKey(key);
        final Method m = component.getAccessor();
        try {
            m.setAccessible(true);
        } catch (Exception ignored) { }
        try {
            return m.invoke(instance);
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new AssertionError("Unexpected checked exception in record accessor", cause);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unexpected reflection error", e);
        }
    }

    @Override
    public void applyTableComments(@NotNull Comments comments) {
        applyAnnotationComments(this.clazz.handle(), comments);
    }

    @Override
    public void applyFieldComments(@NotNull Key key, @NotNull Comments comments) {
        final RecordComponent component = unwrapRecordComponentKey(key);
        applyAnnotationComments(component.getAccessor(), comments);
    }

    //

    private static final class Builder<O> implements TableTypeModel.Builder<O> {

        private final RecordTableTypeModel<O> parent;
        private final Object[] values;

        private Builder(@NotNull RecordTableTypeModel<O> parent) {
            this.parent = parent;
            this.values = new Object[parent.components.length];
        }

        //

        @Override
        public void set(@NotNull Key key, @NotNull Object value) {
            final RecordComponent component = unwrapRecordComponentKey(key);
            this.values[component.index()] = value;
        }

        @Override
        public @NotNull O build() {
            Constructor<O> con = this.parent.clazz.getPrimaryConstructor();
            try {
                con.setAccessible(true);
            } catch (Exception ignored) { }

            try {
                return con.newInstance(this.values);
            } catch (InvocationTargetException | ExceptionInInitializerError e) {
                Throwable cause = e.getCause();
                if (cause == null) cause = e;
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                throw new AssertionError("Unexpected checked exception in record constructor", cause);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Unexpected reflection error", e);
            }
        }

    }

    private static final class RecordComponentKey extends MemberKey<Method> {

        private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = definePrimitiveDefaults(
                Byte.TYPE, (byte) 0,
                Short.TYPE, (short) 0,
                Integer.TYPE, 0,
                Long.TYPE, 0L,
                Float.TYPE, 0f,
                Double.TYPE, 0d,
                Character.TYPE, (char) 0,
                Boolean.TYPE, Boolean.FALSE
        );

        @SuppressWarnings("SameParameterValue")
        private static Map<Class<?>, Object> definePrimitiveDefaults(
               Object... entries
        ) {
            int len = entries.length;
            assert (len & 1) == 0;
            len >>= 1;

            Map<Class<?>, Object> map = new HashMap<>((len * 4) / 3 + 1);
            int head = 0;
            for (int i = 0; i < len; i++) {
                Class<?> key = (Class<?>) entries[head++];
                Object value = entries[head++];
                map.put(key, value);
            }

            return Collections.unmodifiableMap(map);
        }

        //

        private final RecordComponent component;

        RecordComponentKey(
                @NotNull RecordComponent component,
                @NotNull KeyConvention defaultConvention
        ) {
            super(component.getAccessor(), defaultConvention);
            this.component = component;
        }

        //

        @Override
        protected Class<?> typeClassOf(Method member) {
            return member.getReturnType();
        }

        @Override
        protected @Nullable Object nonSpecificDefault(Class<?> type) {
            return PRIMITIVE_DEFAULTS.get(type);
        }

    }

}
