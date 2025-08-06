package io.github.wasabithumb.jtoml.serial.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Annotation for use in {@link io.github.wasabithumb.jtoml.serial.TomlSerializable reflect serialization}.
 * Overrides the TOML key which the annotated field or record component maps to. By default, the key is
 * equal to the name of the field.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
public @interface Key {
    @NotNull String value();
}
