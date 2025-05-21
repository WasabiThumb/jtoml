package io.github.wasabithumb.jtoml.serial;

/**
 * <p>
 *     Marker interface for classes which may be serialized
 *     with the reflection serializer.
 *     Requires {@code jtoml-serializer-reflect} to work as intended.
 * </p>
 * <p>
 *     When de/serializing a {@link TomlSerializable} type, all
 *     declared non-transient fields are considered. Such fields
 *     declared in superclasses are also considered, as long as each
 *     superclass is itself marked as {@link TomlSerializable}
 *     either directly or by inheritance.
 * </p>
 * <p>
 *     A {@link TomlSerializable} type that is not a <a href="https://openjdk.org/jeps/395">record</a>
 *     must have a no-args constructor. If the type is a record, it should not implement the
 *     {@link TomlSerializable} marker interface. This marker is always ignored for records.
 * </p>
 */
public interface TomlSerializable { }
