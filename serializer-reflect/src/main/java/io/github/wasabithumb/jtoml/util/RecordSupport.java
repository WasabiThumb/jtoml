package io.github.wasabithumb.jtoml.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * Reflection for Java 14 Records
 * implemented in Java 8
 */
@ApiStatus.Internal
public final class RecordSupport {

    private static final boolean OK;
    private static final Class<?> C_RECORD;
    private static final Method M_CLASS_GET_RECORD_COMPONENTS;
    private static final Method M_RECORD_COMPONENT_GET_NAME;
    private static final Method M_RECORD_COMPONENT_GET_TYPE;
    private static final Method M_RECORD_COMPONENT_GET_GENERIC_TYPE;
    private static final Method M_RECORD_COMPONENT_GET_ACCESSOR;
    private static final Method M_RECORD_COMPONENT_GET_DECLARED_ANNOTATIONS;

    static {
        boolean ok = true;
        Class<?> cRecord = null;
        Class<?> cRecordComponent = null;
        Method mClassGetRecordComponents = null;
        Method mRecordComponentGetName = null;
        Method mRecordComponentGetType = null;
        Method mRecordComponentGetGenericType = null;
        Method mRecordComponentGetAccessor = null;
        Method mRecordComponentGetDeclaredAnnotations = null;

        try {
            cRecord = Class.forName("java.lang.Record");
            cRecordComponent = Class.forName("java.lang.reflect.RecordComponent");
        } catch (ClassNotFoundException ignored) {
            ok = false;
        }

        if (ok) {
            try {
                //noinspection JavaReflectionMemberAccess
                mClassGetRecordComponents = Class.class.getMethod("getRecordComponents");
                mRecordComponentGetName = cRecordComponent.getMethod("getName");
                mRecordComponentGetType = cRecordComponent.getMethod("getType");
                mRecordComponentGetGenericType = cRecordComponent.getMethod("getGenericType");
                mRecordComponentGetAccessor = cRecordComponent.getMethod("getAccessor");
                mRecordComponentGetDeclaredAnnotations = cRecordComponent.getMethod("getDeclaredAnnotations");
            } catch (NoSuchMethodException ignored) {
                ok = false;
            }
        }

        OK = ok;
        C_RECORD = cRecord;
        M_CLASS_GET_RECORD_COMPONENTS = mClassGetRecordComponents;
        M_RECORD_COMPONENT_GET_NAME = mRecordComponentGetName;
        M_RECORD_COMPONENT_GET_TYPE = mRecordComponentGetType;
        M_RECORD_COMPONENT_GET_GENERIC_TYPE = mRecordComponentGetGenericType;
        M_RECORD_COMPONENT_GET_ACCESSOR = mRecordComponentGetAccessor;
        M_RECORD_COMPONENT_GET_DECLARED_ANNOTATIONS = mRecordComponentGetDeclaredAnnotations;
    }

    //

    private static void checkOk() {
        if (!OK) throw new IllegalStateException("JRE does not support records");
    }

    private static @UnknownNullability Object invoke(@NotNull Object o, @NotNull Method m) {
        try {
            return m.invoke(o);
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new AssertionError("Unexpected error", e);
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new AssertionError("Unexpected reflection error", e);
        }
    }

    public static boolean isRecord(@NotNull Class<?> cls) {
        if (!OK) return false;
        return C_RECORD.isAssignableFrom(cls);
    }

    public static @NotNull Component @UnknownNullability [] getRecordComponents(@NotNull Class<?> cls) {
        checkOk();

        Object rcs = invoke(cls, M_CLASS_GET_RECORD_COMPONENTS);
        if (rcs == null) return null;

        int len = Array.getLength(rcs);
        Component[] ret = new Component[len];
        for (int i=0; i < len; i++)
            ret[i] = new Component(i, Array.get(rcs, i));

        return ret;
    }

    public static <T> @NotNull T createRecord(@NotNull Class<T> cls, @UnknownNullability Object @NotNull [] args) {
        checkOk();

        Object rcs = invoke(cls, M_CLASS_GET_RECORD_COMPONENTS);
        if (rcs == null) {
            throw new IllegalArgumentException(cls.getName() + " is not a record class");
        }

        int len = Array.getLength(rcs);
        if (len != args.length) {
            throw new IllegalArgumentException("Incorrect number of record components (expected " + len +
                    ", got " + args.length);
        }

        Class<?>[] types = new Class<?>[len];
        for (int i=0; i < len; i++) {
            types[i] = (Class<?>) invoke(Array.get(rcs, i), M_RECORD_COMPONENT_GET_TYPE);
        }

        Constructor<?> con;
        try {
            con = cls.getConstructor(types);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Canonical constructor for record " + cls.getName() +
                    " does not exist", e);
        }

        Object ret;
        try {
            ret = con.newInstance(args);
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IllegalStateException(
                    "Canonical constructor for record " + cls.getName() + " raised an exception",
                    e
            );
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new AssertionError("Unexpected reflection error", e);
        }

        return cls.cast(ret);
    }

    //

    public static final class Component {

        private final int index;
        private final Object handle;

        private Component(int index, @NotNull Object handle) {
            this.index = index;
            this.handle = handle;
        }

        //

        public int index() {
            return this.index;
        }

        public @NotNull String name() {
            return (String) invoke(this.handle, M_RECORD_COMPONENT_GET_NAME);
        }

        public @NotNull ParameterizedClass<?> type() {
            Class<?> ta = (Class<?>) invoke(this.handle, M_RECORD_COMPONENT_GET_TYPE);
            Type tb = (Type) invoke(this.handle, M_RECORD_COMPONENT_GET_GENERIC_TYPE);
            return new ParameterizedClass<>(ta, tb);
        }

        public @NotNull Method accessor() {
            return (Method) invoke(this.handle, M_RECORD_COMPONENT_GET_ACCESSOR);
        }

        public @NotNull Object access(@NotNull Object record) {
            return invoke(record, this.accessor());
        }

        public @NotNull Annotation @NotNull [] declaredAnnotations() {
            return (Annotation[]) invoke(this.handle, M_RECORD_COMPONENT_GET_DECLARED_ANNOTATIONS);
        }

        @Override
        public int hashCode() {
            return this.handle.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Component)) return false;
            return this.handle.equals(((Component) obj).handle);
        }

        @Override
        public @NotNull String toString() {
            return this.handle.toString();
        }

    }

}
