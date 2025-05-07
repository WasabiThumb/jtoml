package io.github.wasabithumb.jtoml.value.primitive;

/**
 * Type of a {@link TomlPrimitive TOML primitive};
 * values that are not a table or array
 */
public enum TomlPrimitiveType {
    STRING,
    BOOLEAN,
    INTEGER,
    FLOAT,
    OFFSET_DATE_TIME,
    LOCAL_DATE_TIME,
    LOCAL_DATE,
    LOCAL_TIME
}
