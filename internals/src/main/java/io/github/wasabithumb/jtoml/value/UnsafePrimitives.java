package io.github.wasabithumb.jtoml.value;

import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Experimental
public final class UnsafePrimitives {

    private static final Constructor<?> FLOAT_WITH_CHARS;
    static {
        Class<?> cls;
        Constructor<?> con = null;
        try {
            cls = Class.forName("io.github.wasabithumb.jtoml.value.primitive.FloatTomlPrimitive");
            con = cls.getDeclaredConstructor(Double.TYPE, String.class);
            con.setAccessible(true);
        } catch (ReflectiveOperationException | SecurityException e) {
            Logger.getLogger("jtoml")
                    .log(Level.WARNING, "Failed to access float constructor (please report this)", e);
        }
        FLOAT_WITH_CHARS = con;
    }

    //

    @Contract("_, _ -> new")
    public static @NotNull TomlPrimitive createFloat(double v, @NotNull String chars) {
        if (FLOAT_WITH_CHARS == null) return TomlPrimitive.of(v);
        TomlPrimitive ret;
        try {
            ret = (TomlPrimitive) FLOAT_WITH_CHARS.newInstance(v, chars);
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw ((RuntimeException) cause);
            throw new AssertionError("Unexpected error in constructor/initializer", e);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unexpected reflection error", e);
        }
        return ret;
    }

    //

    private UnsafePrimitives() { }

}
