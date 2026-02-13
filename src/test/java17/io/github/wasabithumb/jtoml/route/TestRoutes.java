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

package io.github.wasabithumb.jtoml.route;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class TestRoutes {

    private static final List<Class<? extends TestRoute>> IMPL_CLASSES = findImplClasses();

    //

    public static Stream<TestRoute> stream() {
        return IMPL_CLASSES.stream()
                .map(TestRoutes::initImpl);
    }

    private static TestRoute initImpl(Class<? extends TestRoute> cls) {
        Constructor<? extends TestRoute> ctor;
        try {
            ctor = cls.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Route class " + cls.getName() + " has no primary constructor", e);
        }

        try {
            ctor.setAccessible(true);
        } catch (InaccessibleObjectException ignored) { }

        TestRoute instance;
        try {
            instance = ctor.newInstance();
        } catch (ExceptionInInitializerError | InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException re) throw re;
            throw new IllegalStateException("Failed to initialize route class " + cls.getName(), cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unexpected reflection error", e);
        }

        return instance;
    }

    private static List<Class<? extends TestRoute>> findImplClasses() {
        Path codeSource = codeSource();
        List<Class<? extends TestRoute>> ret = new LinkedList<>();
        String packageName = TestRoute.class.getPackageName() + ".impl";
        Consumer<String> resolver = classResolver(TestRoute.class, ret);

        if (Files.isDirectory(codeSource)) {
            topLevelClassesDir(codeSource, packageName, resolver);
        } else {
            topLevelClassesJar(codeSource, packageName, resolver);
        }

        return List.copyOf(ret);
    }

    private static Path codeSource() {
        URL location = TestRoutes.class.getProtectionDomain().getCodeSource().getLocation();
        URI uri;
        try {
            uri = location.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Code source is not a valid URI", e);
        }
        return (new File(uri)).toPath();
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> Consumer<String> classResolver(
            Class<T> superType,
            List<Class<? extends T>> out
    ) {
        final ClassLoader cl = TestRoutes.class.getClassLoader();
        return ((String name) -> {
            Class<?> cls;
            try {
                cls = Class.forName(name, false, cl);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to resolve discovered class", e);
            }
            if (cls.isInterface()) return;
            if (Modifier.isAbstract(cls.getModifiers())) return;
            if (!superType.isAssignableFrom(cls)) return;
            out.add(cls.asSubclass(superType));
        });
    }

    private static void topLevelClassesDir(Path dir, String packageName, Consumer<String> out) {
        dir = resolvePackage(dir, packageName, Path::resolve);
        try (Stream<Path> stream = Files.list(dir)) {
            Iterator<Path> iter = stream.iterator();
            Path next;

            while (iter.hasNext()) {
                next = iter.next();
                if (!Files.isRegularFile(next)) continue;

                Path nameElement = next.getFileName();
                if (nameElement == null) continue;

                String name = nameElement.toString();
                if (!name.endsWith(".class")) continue;
                if (name.indexOf('$') != -1) continue;

                out.accept(packageName + "." + name.substring(0, name.length() - 6));
            }
        } catch (IOException e) {
            throw wrapSourceIOException(dir, e);
        } catch (UncheckedIOException e) {
            throw wrapSourceIOException(dir, e.getCause());
        }
    }

    private static void topLevelClassesJar(Path jar, String packageName, Consumer<String> out) {
        final String prefix = resolvePackage(
                new StringBuilder(),
                packageName,
                (StringBuilder sb, String part) -> sb.append(part).append('/')
        ).toString();

        try (InputStream in = Files.newInputStream(jar);
             ZipInputStream zin = new ZipInputStream(in)
        ) {
            ZipEntry next;
            while ((next = zin.getNextEntry()) != null) {
                topLevelClassesJar0(next, packageName, prefix, out);
            }
        } catch (IOException e) {
            throw wrapSourceIOException(jar, e);
        }
    }

    private static void topLevelClassesJar0(
            ZipEntry entry,
            String packageName,
            String prefix,
            Consumer<String> out
    ) {
        String name = entry.getName();
        int nameLen = name.length();
        int prefixLen = prefix.length();

        if (nameLen <= prefixLen) return;
        for (int i = 0; i < prefixLen; i++) {
            if (name.charAt(i) != prefix.charAt(i)) return;
        }

        String sub = null;
        int q = -1;
        char c;
        for (int i = prefixLen; i < nameLen; i++) {
            c = name.charAt(i);
            if (q != -1) {
                char x = "class".charAt(q++);
                if (c != x) return;
            } else if (c == '/' || c == '$') {
                return;
            } else if (c == '.') {
                if (i != (nameLen - 6)) return;
                sub = name.substring(prefixLen, i);
                q = 0;
            }
        }

        if (sub != null) {
            out.accept(packageName + "." + sub);
        }
    }

    private static <T> T resolvePackage(T seed, String packageName, BiFunction<T, String, T> operator) {
        final int len = packageName.length();
        T ret = seed;
        int start = 0;

        for (int i = 0; i < len; i++) {
            if (packageName.charAt(i) != '.') continue;
            if (start != i) ret = operator.apply(ret, packageName.substring(start, i));
            start = i + 1;
        }

        if (start != len) {
            ret = operator.apply(ret, packageName.substring(start, len));
        }

        return ret;
    }

    private static IllegalStateException wrapSourceIOException(Path source, IOException error) {
        throw new IllegalStateException("Failed to read contents of " + source.toAbsolutePath(), error);
    }

    //

    private TestRoutes() { }

}
