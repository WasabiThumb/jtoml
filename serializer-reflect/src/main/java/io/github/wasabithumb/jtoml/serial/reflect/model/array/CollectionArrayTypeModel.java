package io.github.wasabithumb.jtoml.serial.reflect.model.array;

import io.github.wasabithumb.jtoml.util.ParameterizedClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

@ApiStatus.Internal
abstract class CollectionArrayTypeModel<T extends Collection<E>, E> implements ArrayTypeModel<T> {

    @Contract("_, _ -> new")
    protected static <C extends Collection<?>> C autoConstruct(@NotNull Class<C> clazz, int length) {
        Constructor<?> con;
        boolean withLength;

        try {
            con = clazz.getConstructor(Integer.TYPE);
            withLength = true;
        } catch (NoSuchMethodException e1) {
            try {
                con = clazz.getConstructor();
                withLength = false;
            } catch (NoSuchMethodException e2) {
                IllegalStateException ex = new IllegalStateException(
                        "No suitable constructor for collection type " +
                                clazz.getName()
                );
                ex.addSuppressed(e1);
                ex.addSuppressed(e2);
                throw ex;
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
            throw new IllegalStateException("Failed to invoke constructor for collection type " + clazz.getName(), cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unexpected reflection error", e);
        }

        return clazz.cast(ret);
    }

    //

    protected final Class<T> type;
    protected final ParameterizedClass<E> elementType;

    CollectionArrayTypeModel(
            @NotNull Class<T> type,
            @NotNull ParameterizedClass<E> elementType
    ) {
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
    public int size(@NotNull T instance) {
        return instance.size();
    }

    @Override
    public @NotNull Iterator<?> iterator(@NotNull T instance) {
        return instance.iterator();
    }

    @Override
    public void put(@NotNull T instance, @NotNull Object object) {
        instance.add(this.elementType.raw().cast(object));
    }

}
