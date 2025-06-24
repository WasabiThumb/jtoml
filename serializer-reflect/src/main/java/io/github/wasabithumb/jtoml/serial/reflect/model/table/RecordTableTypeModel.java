package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.recsup.RecordClass;
import io.github.wasabithumb.recsup.RecordComponent;
import io.github.wasabithumb.recsup.RecordSupport;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@ApiStatus.Internal
final class RecordTableTypeModel<T> extends AbstractTableTypeModel<T> {

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
    public @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull T instance) {
        List<TomlKey> ret = new ArrayList<>(this.components.length);
        for (RecordComponent c : this.components) {
            ret.add(TomlKey.literal(c.getName()));
        }
        return Collections.unmodifiableList(ret);
    }

    private @NotNull RecordComponent lookupComponent(@NotNull TomlKey key) {
        if (key.size() != 1)
            throw new IllegalArgumentException("Invalid key size (expected 1, got " + key.size() + ")");

        String k0 = key.get(0);

        for (RecordComponent rc : this.components) {
            if (k0.equals(rc.getName())) {
                return rc;
            }
        }

        throw new IllegalArgumentException("Record " + this.clazz.handle().getName() + " has no component named " + k0);
    }

    @Override
    public @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key) {
        RecordComponent rc = this.lookupComponent(key);
        return new ParameterizedClass<>(rc.getType(), rc.getGenericType());
    }

    @Override
    public @UnknownNullability Object get(@NotNull T instance, @NotNull TomlKey key) {
        Method m = this.lookupComponent(key).getAccessor();
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
        applyAnnotationComments(this.clazz.handle().getDeclaredAnnotations(), comments);
    }

    @Override
    public void applyFieldComments(@NotNull TomlKey key, @NotNull Comments comments) {
        RecordComponent component = this.lookupComponent(key);
        applyAnnotationComments(component.getAccessor().getDeclaredAnnotations(), comments);
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
        public void set(@NotNull TomlKey key, @NotNull Object value) {
            RecordComponent component = this.parent.lookupComponent(key);
            this.values[component.index()] = value;
        }

        @Override
        public @NotNull O build() {
            try {
                return this.parent.clazz.getPrimaryConstructor().newInstance(this.values);
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

}
