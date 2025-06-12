package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import io.github.wasabithumb.jtoml.util.RecordSupport;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
final class RecordTableTypeModel<T> extends AbstractTableTypeModel<T> {

    private final Class<T> clazz;
    private final RecordSupport.Component[] components;

    RecordTableTypeModel(@NotNull Class<T> clazz) {
        this.clazz = clazz;
        this.components = RecordSupport.getRecordComponents(clazz);
    }

    //

    @Override
    public @NotNull Class<T> type() {
        return this.clazz;
    }

    @Override
    public @NotNull TableTypeModel.Builder<T> create() {
        return new Builder<>(this);
    }

    @Override
    public @NotNull @Unmodifiable Collection<TomlKey> keys(@NotNull T instance) {
        List<TomlKey> ret = new ArrayList<>(this.components.length);
        for (RecordSupport.Component c : this.components) {
            ret.add(TomlKey.literal(c.name()));
        }
        return Collections.unmodifiableList(ret);
    }

    private @NotNull RecordSupport.Component lookupComponent(@NotNull TomlKey key) {
        if (key.size() != 1)
            throw new IllegalArgumentException("Invalid key size (expected 1, got " + key.size() + ")");

        String k0 = key.get(0);

        for (RecordSupport.Component rc : this.components) {
            if (k0.equals(rc.name())) {
                return rc;
            }
        }

        throw new IllegalArgumentException("Record " + this.clazz.getName() + " has no component named " + k0);
    }

    @Override
    public @NotNull ParameterizedClass<?> elementType(@NotNull TomlKey key) {
        return this.lookupComponent(key).type();
    }

    @Override
    public @UnknownNullability Object get(@NotNull T instance, @NotNull TomlKey key) {
        return this.lookupComponent(key).access(instance);
    }

    @Override
    public void applyTableComments(@NotNull Comments comments) {
        applyAnnotationComments(this.clazz.getDeclaredAnnotations(), comments);
    }

    @Override
    public void applyFieldComments(@NotNull TomlKey key, @NotNull Comments comments) {
        RecordSupport.Component component = this.lookupComponent(key);
        applyAnnotationComments(component.declaredAnnotations(), comments);
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
            RecordSupport.Component component = this.parent.lookupComponent(key);
            this.values[component.index()] = value;
        }

        @Override
        public @NotNull O build() {
            return RecordSupport.createRecord(this.parent.clazz, this.values);
        }

    }

}
