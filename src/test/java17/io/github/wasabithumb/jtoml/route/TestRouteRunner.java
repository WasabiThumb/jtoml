package io.github.wasabithumb.jtoml.route;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

public record TestRouteRunner(
        TestRoute route
) implements Executable {

    private static final JToml DEFAULT_INSTANCE = JToml.jToml();

    public static DynamicTest newDynamicTest(TestRoute route) {
        final TestRouteRunner runner = new TestRouteRunner(route);
        return DynamicTest.dynamicTest(runner.route.displayName(), runner);
    }

    //

    @Override
    public void execute() {
        JToml instance;
        if (this.route instanceof TestRoute.Configuring configuring) {
            JTomlOptions.Builder options = JTomlOptions.builder();
            configuring.configure(options);
            instance = JToml.jToml(options.build());
        } else {
            instance = DEFAULT_INSTANCE;
        }

        loadSentinelFields(instance, this.route);
        this.route.execute(instance);
    }

    private static void loadSentinelFields(JToml toml, Object instance) {
        findSentinelFields(
                instance.getClass(),
                (Field field, String path) -> assertDoesNotThrow(() -> loadSentinelField(toml, instance, field, path))
        );
    }

    private static void loadSentinelField(
            JToml toml,
            Object instance,
            Field field,
            String path
    ) throws IOException {
        String fullPath = "/sentinel/" + path;
        TomlDocument doc;

        try (InputStream in = TestRouteRunner.class.getResourceAsStream(fullPath)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Cannot populate field \"" + field.getName() +
                                "\" in class " + field.getDeclaringClass().getName() +
                                " (resource not found)"
                );
            }
            doc = toml.read(in);
        }

        Class<?> type = field.getType();
        Object value;
        if (type.isAssignableFrom(TomlDocument.class)) {
            value = doc;
        } else {
            value = toml.fromToml(type, doc);
        }

        Throwable suppressed = null;
        try {
            field.setAccessible(true);
        } catch (InaccessibleObjectException e) {
            suppressed = e;
        }

        try {
            field.set(instance, value);
        } catch (ReflectiveOperationException e) {
            if (suppressed != null) e.addSuppressed(suppressed);
            throw new IllegalStateException(
                    "Failed to populate field \"" + field.getName() +
                            "\" in class " + field.getDeclaringClass().getName(),
                    e
            );
        }
    }

    private static void findSentinelFields(Class<?> type, BiConsumer<Field, String> fn) {
        do {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                Sentinel annotation = field.getAnnotation(Sentinel.class);
                if (annotation == null) continue;
                fn.accept(field, annotation.value());
            }
            type = type.getSuperclass();
        } while (type != null && TestRoute.class.isAssignableFrom(type));
    }

}
