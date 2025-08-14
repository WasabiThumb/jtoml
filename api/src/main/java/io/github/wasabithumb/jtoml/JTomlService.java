package io.github.wasabithumb.jtoml;

import io.github.wasabithumb.jtoml.option.JTomlOptions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.ServiceLoader;

@ApiStatus.Internal
public abstract class JTomlService {

    private static JTomlService SERVICE = null;

    static synchronized @NotNull JTomlService get() {
        JTomlService ret = SERVICE;
        if (ret == null) {
            ret = load();
            SERVICE = ret;
        }
        return ret;
    }

    private static @NotNull JTomlService load() {
        // First try with context class loader
        ServiceLoader<JTomlService> loader = ServiceLoader.load(JTomlService.class);
        Iterator<JTomlService> iter = loader.iterator();

        if (!iter.hasNext()) {
            // Then try with known class loader
            loader = ServiceLoader.load(JTomlService.class, JToml.class.getClassLoader());
            iter = loader.iterator();
            if (!iter.hasNext()) {
                throw new IllegalStateException("No JToml provider could be found (bad build config?)");
            }
        }

        JTomlService ret = iter.next();
        if (iter.hasNext()) {
            throw new IllegalStateException("Multiple JToml providers found (broken relocation?)");
        }
        return ret;
    }

    //

    @Contract(pure = true)
    @ApiStatus.OverrideOnly
    public abstract @NotNull JToml defaultInstance();

    @Contract("_ -> new")
    @ApiStatus.OverrideOnly
    public abstract @NotNull JToml createInstance(@NotNull JTomlOptions options);

}
