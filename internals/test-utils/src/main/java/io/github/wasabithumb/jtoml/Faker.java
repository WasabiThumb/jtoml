package io.github.wasabithumb.jtoml;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A utility class for generating sample values for the purpose
 * of round-trip tests. Named in reference to the {@code faker.js} project.
 */
public final class Faker {

    private static final Random RANDOM = new Random();
    private static final List<String> NOUNS = readStringList("faker/nouns.txt");
    private static final List<String> ADJECTIVES = readStringList("faker/adjectives.txt");
    private static final List<String> EMOJI = readStringList("faker/emoji.txt");

    //

    /**
     * Populates the fields of a POJO with sample values
     */
    public static void populate(@NotNull Object object) {
        Class<?> cls = object.getClass();
        for (Field f : cls.getDeclaredFields()) {
            try {
                f.setAccessible(true);
            } catch (Exception ignored) { }

            int mod = f.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isTransient(mod)) continue;

            Object value = create(f.getType());
            try {
                f.set(object, value);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to set field \"" + f.getName() + "\" on POJO");
            }
        }
    }

    /**
     * Creates a new sample value of the given type
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull T create(@NotNull Class<T> clazz) {
        if (String.class.equals(clazz)) {
            return clazz.cast(createString());
        } else if (Boolean.class.equals(clazz) || Boolean.TYPE.equals(clazz)) {
            return (T) Boolean.valueOf(createBoolean());
        } else if (Long.class.equals(clazz) || Long.TYPE.equals(clazz)) {
            return (T) Long.valueOf(createInteger());
        } else if (Integer.class.equals(clazz) || Integer.TYPE.equals(clazz)) {
            return (T) Integer.valueOf((int) createInteger());
        } else if (Short.class.equals(clazz) || Short.TYPE.equals(clazz)) {
            return (T) Short.valueOf((short) createInteger());
        } else if (Byte.class.equals(clazz) || Byte.TYPE.equals(clazz)) {
            return (T) Byte.valueOf((byte) createInteger());
        } else if (Double.class.equals(clazz) || Double.TYPE.equals(clazz)) {
            return (T) Double.valueOf(createFloat());
        } else if (Float.class.equals(clazz) || Float.TYPE.equals(clazz)) {
            return (T) Float.valueOf((float) createFloat());
        } else if (LocalDate.class.equals(clazz)) {
            return clazz.cast(createLocalDate());
        } else if (LocalTime.class.equals(clazz)) {
            return clazz.cast(createLocalTime());
        } else if (LocalDateTime.class.equals(clazz)) {
            return clazz.cast(createLocalDateTime());
        } else if (OffsetDateTime.class.equals(clazz)) {
            return clazz.cast(createOffsetDateTime());
        } else {
            // Assume POJO
            return createObject(clazz);
        }
    }

    public static @NotNull String createString() {
        return selectString(ADJECTIVES) + " " + selectString(NOUNS) + " " + selectString(EMOJI);
    }

    public static boolean createBoolean() {
        return RANDOM.nextBoolean();
    }

    public static long createInteger() {
        return RANDOM.nextLong(200000L) - 100000L;
    }

    public static double createFloat() {
        return (RANDOM.nextDouble() * 100d) - 50d;
    }

    public static @NotNull LocalDate createLocalDate() {
        final int year = 1800 + RANDOM.nextInt(400);
        Month m = Month.of(RANDOM.nextInt(12) + 1);
        int day = RANDOM.nextInt(m.length(Year.of(year).isLeap())) + 1;
        return LocalDate.of(year, m, day);
    }

    public static @NotNull LocalTime createLocalTime() {
        return LocalTime.of(
                RANDOM.nextInt(24),
                RANDOM.nextInt(60),
                RANDOM.nextInt(60),
                RANDOM.nextInt(1000) * 1000000
        );
    }

    public static @NotNull LocalDateTime createLocalDateTime() {
        return LocalDateTime.of(createLocalDate(), createLocalTime());
    }

    public static @NotNull OffsetDateTime createOffsetDateTime() {
        return OffsetDateTime.of(
                createLocalDateTime(),
                ZoneOffset.ofTotalSeconds((RANDOM.nextInt(73) * 1800) - 64800)
        );
    }

    private static <T> @NotNull T createObject(@NotNull Class<T> clazz) {
        int mod = clazz.getModifiers();
        if (Modifier.isAbstract(mod) || Modifier.isInterface(mod)) {
            throw new IllegalArgumentException("Type \"" + clazz +
                    "\" is not a primitive nor is directly instantiable");
        }

        Constructor<T> con;
        try {
            con = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Type \"" + clazz.getName() +
                    "\" is not a primitive nor has a primary constructor");
        }

        try {
            con.setAccessible(true);
        } catch (Exception ignored) { }

        T instance;
        try {
            instance = con.newInstance();
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IllegalStateException(
                    "Failed to invoke primary constructor for type \"" + clazz.getName() + "\"",
                    cause
            );
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unexpected reflection error", e);
        }

        populate(instance);
        return instance;
    }

    private static @NotNull String selectString(@NotNull List<String> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

    private static @NotNull List<String> readStringList(@NotNull String path) {
        try (InputStream is = Faker.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("Resource \"" + path + "\" not found");
            List<String> list = new ArrayList<>();
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    list.add(line);
                }
            }
            return Collections.unmodifiableList(list);
        } catch (IOException e) {
            throw new AssertionError("Failed to read resource @ " + path);
        }
    }

}
