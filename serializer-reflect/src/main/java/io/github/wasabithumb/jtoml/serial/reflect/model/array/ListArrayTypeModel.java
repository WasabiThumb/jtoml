package io.github.wasabithumb.jtoml.serial.reflect.model.array;

import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@ApiStatus.Internal
final class ListArrayTypeModel<T extends List<E>, E> implements ArrayTypeModel<T> {

    @SuppressWarnings("unchecked")
    static <IT extends List<IE>, IE> ListArrayTypeModel<IT, IE> create(
            @NotNull Class<IT> listType,
            @NotNull ParameterizedClass<?> elementType
    ) {
        return new ListArrayTypeModel<>(listType, (ParameterizedClass<IE>) elementType);
    }

    //

    private final Class<T> type;
    private final ParameterizedClass<E> elementType;

    private ListArrayTypeModel(@NotNull Class<T> type, @NotNull ParameterizedClass<E> elementType) {
        this.type = type;
        this.elementType = elementType;
    }

    //

    @Override
    public @NotNull Class<T> type() {
        return this.type;
    }

    @Override
    public @NotNull ParameterizedClass<?> componentType() {
        return this.elementType;
    }

    @Override
    public @NotNull T createNew(int length) {
        if (this.type.isAssignableFrom(ArrayList.class))
            return this.type.cast(new ArrayList<E>(length));

        if (this.type.isAssignableFrom(LinkedList.class))
            return this.type.cast(new LinkedList<E>());

        Constructor<?> con;
        boolean withLength;

        try {
            con = this.type.getConstructor(Integer.TYPE);
            withLength = true;
        } catch (NoSuchMethodException e1) {
            try {
                con = this.type.getConstructor();
                withLength = false;
            } catch (NoSuchMethodException e2) {
                IllegalStateException e = new IllegalStateException("No suitable constructor for list type " +
                        this.type.getName());
                e.addSuppressed(e1);
                e.addSuppressed(e2);
                throw e;
            }
        }

        Object ret;
        try {
            ret = withLength ? con.newInstance(length) :
                    con.newInstance();
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IllegalStateException("Unexpected error in list constructor", cause);
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new IllegalStateException("Unexpected reflection error", e);
        }

        return this.type.cast(ret);
    }

    @Override
    public int size(@NotNull T instance) {
        return instance.size();
    }

    @Override
    public @UnknownNullability Object get(@NotNull T instance, int index) {
        return instance.get(index);
    }

    @Override
    public void set(@NotNull T instance, int index, @NotNull Object object) {
        if (index == instance.size()) {
            instance.add(this.elementType.raw().cast(object));
        } else {
            instance.set(index, this.elementType.raw().cast(object));
        }
    }
    
}
