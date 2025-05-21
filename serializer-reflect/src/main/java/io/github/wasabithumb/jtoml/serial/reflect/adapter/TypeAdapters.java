package io.github.wasabithumb.jtoml.serial.reflect.adapter;

import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.github.wasabithumb.jtoml.serial.reflect.adapter.TypeAdapter.*;

@ApiStatus.Internal
public final class TypeAdapters {

    private static final Map<Class<?>, TypeAdapter<?>> MAP;
    static {
        Map<Class<?>, TypeAdapter<?>> map = new HashMap<>();
        TypeAdapter<?> tmp;

        // Boolean
        tmp = create(
                Boolean.class,
                TomlPrimitive::asBoolean,
                TomlPrimitive::of
        );
        map.put(Boolean.class, tmp);
        map.put(Boolean.TYPE, tmp);

        // Byte
        tmp = create(
                Byte.class,
                (TomlPrimitive v) -> (byte) v.asInteger(),
                (Byte b) -> TomlPrimitive.of(b.intValue())
        );
        map.put(Byte.class, tmp);
        map.put(Byte.TYPE, tmp);

        // Short
        tmp = create(
                Short.class,
                (TomlPrimitive v) -> (short) v.asInteger(),
                (Short s) -> TomlPrimitive.of(s.intValue())
        );
        map.put(Short.class, tmp);
        map.put(Short.TYPE, tmp);

        // Integer
        tmp = create(
                Integer.class,
                TomlPrimitive::asInteger,
                TomlPrimitive::of
        );
        map.put(Integer.class, tmp);
        map.put(Integer.TYPE, tmp);

        // Long
        tmp = create(
                Long.class,
                TomlPrimitive::asLong,
                TomlPrimitive::of
        );
        map.put(Long.class, tmp);
        map.put(Long.TYPE, tmp);

        // Float
        tmp = create(
                Float.class,
                TomlPrimitive::asFloat,
                TomlPrimitive::of
        );
        map.put(Float.class, tmp);
        map.put(Float.TYPE, tmp);

        // Double
        tmp = create(
                Double.class,
                TomlPrimitive::asDouble,
                TomlPrimitive::of
        );
        map.put(Double.class, tmp);
        map.put(Double.TYPE, tmp);

        // Character
        tmp = create(
                Character.class,
                (TomlPrimitive tp) -> {
                    if (tp.isInteger()) {
                        return (char) tp.asInteger();
                    } else {
                        String s = tp.asString();
                        if (s.length() != 1) throw new IllegalStateException("Cannot convert multi-char string to char");
                        return s.charAt(0);
                    }
                },
                (Character c) -> TomlPrimitive.of(Character.toString(c))
        );
        map.put(Character.class, tmp);
        map.put(Character.TYPE, tmp);

        // String
        tmp = create(
                String.class,
                TomlPrimitive::asString,
                TomlPrimitive::of
        );
        map.put(String.class, tmp);

        // Offset Date-Time
        tmp = create(
                OffsetDateTime.class,
                TomlPrimitive::asOffsetDateTime,
                TomlPrimitive::of
        );
        map.put(OffsetDateTime.class, tmp);

        // Local Date-Time
        tmp = create(
                LocalDateTime.class,
                TomlPrimitive::asLocalDateTime,
                TomlPrimitive::of
        );
        map.put(LocalDateTime.class, tmp);

        // Local Date
        tmp = create(
                LocalDate.class,
                TomlPrimitive::asLocalDate,
                TomlPrimitive::of
        );
        map.put(LocalDate.class, tmp);

        // Local Time
        tmp = create(
                LocalTime.class,
                TomlPrimitive::asLocalTime,
                TomlPrimitive::of
        );
        map.put(LocalTime.class, tmp);

        MAP = map;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull TypeAdapter<T> get(@NotNull Class<T> type) {
        TypeAdapter<?> ret = MAP.get(type);
        if (ret == null)
            throw new IllegalArgumentException("No registered adapter for type "  + type.getName());
        return (TypeAdapter<T>) ret;
    }

    //

    private TypeAdapters() { }

}
