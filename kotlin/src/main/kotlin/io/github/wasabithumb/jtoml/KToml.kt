package io.github.wasabithumb.jtoml

import io.github.wasabithumb.jtoml.document.TomlDocument
import io.github.wasabithumb.jtoml.except.TomlException
import io.github.wasabithumb.jtoml.key.TomlKey
import io.github.wasabithumb.jtoml.option.JTomlOptions
import io.github.wasabithumb.jtoml.value.TomlValue
import io.github.wasabithumb.jtoml.value.array.TomlArray
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive
import io.github.wasabithumb.jtoml.value.table.TomlTable
import org.jetbrains.annotations.Contract
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.time.*
import java.util.function.DoubleUnaryOperator
import java.util.function.LongUnaryOperator
import kotlin.jvm.Throws
import kotlin.reflect.KClass


/**
 * A global instance of JToml
 * @see options
 */
object KToml : JToml {

    private var _options: JTomlOptions = JTomlOptions.defaults()
    private var _instance: JToml = JToml.jToml()

    /**
     * The options to use. Setting this should be done with care
     * as this is a global instance. Starts initialized
     * to defaults.
     */
    var options: JTomlOptions
        get() = synchronized(this) { this._options }
        set(value) {
            synchronized(this) {
                this._options = value
                this._instance = JToml.jToml(value)
            }
        }

    private val instance: JToml
        get() = synchronized(this) { this._instance }

    //

    @Throws(TomlException::class)
    override fun readFromString(toml: String): TomlDocument {
        return this.instance.readFromString(toml)
    }

    @Throws(TomlException::class)
    override fun read(inStream: InputStream): TomlDocument {
        return this.instance.read(inStream)
    }

    @Throws(TomlException::class)
    override fun read(reader: Reader): TomlDocument {
        return this.instance.read(reader)
    }

    @Throws(TomlException::class)
    override fun writeToString(table: TomlTable): String {
        return this.instance.writeToString(table)
    }

    @Throws(TomlException::class)
    override fun write(out: OutputStream, table: TomlTable) {
        return this.instance.write(out, table)
    }

    @Throws(TomlException::class)
    override fun write(writer: Writer, table: TomlTable) {
        this.instance.write(writer, table)
    }

    @Throws(TomlException::class)
    override fun <T : Any> fromToml(type: Class<T>, table: TomlTable): T {
        return this.instance.fromToml(type, table)
    }

    @Throws(TomlException::class)
    override fun <T : Any> toToml(type: Class<T>, data: T): TomlTable {
        return this.instance.toToml(type, data)
    }

}

// Serialization

/**
 * Converts the given TOML table to the given type,
 * if an appropriate serializer is present in the classpath
 * @since 1.2.1
 */
@Throws(TomlException::class, IllegalArgumentException::class)
fun <T: Any> JToml.fromToml(type: KClass<T>, table: TomlTable): T {
    return this.fromToml(type.java, table)
}

/**
 * Converts the given object to a TOML table,
 * if an appropriate deserializer is present in the classpath
 * @since 1.2.1
 */
@Throws(TomlException::class, IllegalArgumentException::class)
fun <T: Any> JToml.toToml(type: KClass<T>, data: T): TomlTable {
    return this.toToml(type.java, data)
}

/**
 * Serializes the given TOML table to the given type,
 * if an appropriate serializer is present in the classpath
 */
@Deprecated("Replaced by fromToml")
@Throws(TomlException::class, IllegalArgumentException::class)
fun <T: Any> JToml.serialize(type: KClass<T>, table: TomlTable): T {
    return this.serialize(type.java, table)
}

/**
 * Deserializes a TOML table from the given type,
 * if an appropriate deserializer is present in the classpath
 */
@Deprecated("Replaced by toToml")
@Throws(TomlException::class, IllegalArgumentException::class)
fun <T: Any> JToml.deserialize(type: KClass<T>, data: T): TomlTable {
    return this.deserialize(type.java, data)
}


// Value Coercion

/**
 * Coerces a value into a TomlPrimitive
 * @throws NullPointerException Value is null
 * @throws UnsupportedOperationException Value is not a TomlPrimitive and not of coercible type
 */
val Any?.asTomlPrimitive: TomlPrimitive
    @Contract("null -> fail")
    get() {
        if (this == null)
            throw NullPointerException("Cannot convert null to TOML primitive")

        return when (this) {
            is TomlPrimitive ->  this
            is String ->         TomlPrimitive.of(this)
            is CharSequence ->   TomlPrimitive.of(this.toString())
            is Boolean ->        TomlPrimitive.of(this)
            is Long ->           TomlPrimitive.of(this)
            is Int ->            TomlPrimitive.of(this)
            is Short ->          TomlPrimitive.of(this.toInt())
            is Byte ->           TomlPrimitive.of(this.toInt())
            is ULong ->          TomlPrimitive.of(
                this.toLong()
                    .takeUnless { it < 0L }
                    ?: throw ArithmeticException("ULong ($this) is too large to represent as long")
            )
            is UInt ->           TomlPrimitive.of(this.toLong())
            is UShort ->         TomlPrimitive.of(this.toInt())
            is UByte ->          TomlPrimitive.of(this.toInt())
            is Double ->         TomlPrimitive.of(this)
            is Float ->          TomlPrimitive.of(this)
            is OffsetDateTime -> TomlPrimitive.of(this)
            is LocalDateTime ->  TomlPrimitive.of(this)
            is LocalDate ->      TomlPrimitive.of(this)
            is LocalTime ->      TomlPrimitive.of(this)
            else -> throw UnsupportedOperationException("Target ($this) is not a TOML primitive")
        }
    }

/**
 * Coerces an array into a TomlArray
 * @throws UnsupportedOperationException One or more values could not be converted into a TomlValue
 */
val Array<*>.asTomlArray: TomlArray
    get() {
        val arr = TomlArray.create(this.size)
        for (v in this) arr.add(v.asTomlValue)
        return arr
    }

/**
 * Coerces a collection into a TomlArray
 * @throws UnsupportedOperationException One or more values could not be converted into a TomlValue
 */
val Iterable<*>.asTomlArray: TomlArray
    get() {
        val arr = if (this is Collection)
            TomlArray.create(this.size) else TomlArray.create()
        for (v in this) arr.add(v.asTomlValue)
        return arr
    }

private fun Any?.asTomlKey(parse: Boolean = false): TomlKey {
    if (this is TomlKey) return this
    if (parse) return TomlKey.parse(this.toString())
    return TomlKey.literal(this.toString())
}

/**
 * Coerces a map into a TomlTable.
 * Keys are interpreted as literals and are not parsed.
 * @throws UnsupportedOperationException One or more values could not be converted to a TomlValue
 */
val Map<*, *>.asTomlTable: TomlTable
    get() {
        val table = TomlTable.create()
        for (entry in this.entries) {
            table.put(entry.key.asTomlKey(), entry.value.asTomlValue)
        }
        return table
    }

/**
 * Coerces a value into a TomlValue
 * @throws NullPointerException Value is null
 * @throws UnsupportedOperationException Value is not a TomlValue and not of coercible type
 */
val Any?.asTomlValue: TomlValue
    @Contract("null -> fail")
    get() = when (this) {
        is TomlValue ->   this
        is Map<*, *> ->   this.asTomlTable
        is Iterable<*> -> this.asTomlArray
        is Array<*> ->    this.asTomlArray
        else -> this.asTomlPrimitive
    }

// Primitive Operators

private fun TomlPrimitive.numberArithmetic(
    withInt: LongUnaryOperator? = null,
    withFloat: DoubleUnaryOperator? = null
): TomlPrimitive {
    var f = 0

    if (withInt != null) {
        f = 1
        if (this.isInteger) return TomlPrimitive.of(withInt.applyAsLong(this.asLong()))
        if (this.isOffsetDateTime) {
            var t: Long = this.asOffsetDateTime()
                .toInstant()
                .toEpochMilli()

            t = withInt.applyAsLong(t)

            return TomlPrimitive.of(
                OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(t),
                    this.asOffsetDateTime().offset
                )
            )
        }
        if (this.isLocalDateTime) {
            var t: Long = this.asLocalDateTime()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()

            t = withInt.applyAsLong(t)

            return TomlPrimitive.of(
                LocalDateTime.ofEpochSecond(
                    t / 1000,
                    (t % 1000).toInt() * 1000000,
                    ZoneOffset.UTC
                )
            )
        }
    }

    if (withFloat != null) {
        f += 2
        if (this.isFloat || this.isInteger)
            return TomlPrimitive.of(withFloat.applyAsDouble(this.asDouble()))
    }

    val classifier: String = when (f) {
        0 -> throw IllegalArgumentException("One of withInt, withFloat must be non-null")
        1 -> "integer"
        2 -> "float"
        3 -> "number"
        else -> throw AssertionError()
    }

    throw UnsupportedOperationException("Cannot perform $classifier arithmetic on primitive of type ${this.type()}")
}

private fun TomlPrimitive.floatArithmetic(withFloat: DoubleUnaryOperator): TomlPrimitive {
    return this.numberArithmetic(null, withFloat)
}

/**
 * Alias for !this.asBoolean()
 */
operator fun TomlPrimitive.not(): Boolean {
    return !this.asBoolean()
}

/**
 * Returns a new TomlPrimitive which represents the value of this primitive
 * plus the given number. If the number is a double or float, the returned primitive
 * will be a float primitive. Otherwise, this returns a primitive of the exact same type.
 */
operator fun TomlPrimitive.plus(b: Number): TomlPrimitive {
    return if (b is Double || b is Float) {
        this.floatArithmetic { i -> i + b.toDouble() }
    } else {
        this.numberArithmetic({ i -> i + b.toLong() }, { i -> i + b.toDouble() })
    }
}

/**
 * Returns a new TomlPrimitive which represents the value of this primitive
 * minus the given number. If the number is a double or float, the returned primitive
 * will be a float primitive. Otherwise, this returns a primitive of the exact same type.
 */
operator fun TomlPrimitive.minus(b: Number): TomlPrimitive {
    return if (b is Double || b is Float) {
        this.floatArithmetic { i -> i + b.toDouble() }
    } else {
        this.numberArithmetic({ i -> i - b.toLong() }, { i -> i - b.toDouble() })
    }
}

/**
 * Returns a new TomlPrimitive which represents the value of this primitive
 * multiplied by the given number. If the number is a double or float, the returned primitive
 * will be a float primitive. Otherwise, this returns a primitive of the exact same type.
 */
operator fun TomlPrimitive.times(b: Number): TomlPrimitive {
    return if (b is Double || b is Float) {
        this.floatArithmetic { i -> i * b.toDouble() }
    } else {
        this.numberArithmetic({ i -> i * b.toLong() }, { i -> i * b.toDouble() })
    }
}

/**
 * Returns a new TomlPrimitive which represents the value of this primitive
 * divided by the given number. If the number is a double or float, the returned primitive
 * will be a float primitive. Otherwise, this returns a primitive of the exact same type.
 */
operator fun TomlPrimitive.div(b: Number): TomlPrimitive {
    return if (b is Double || b is Float) {
        this.floatArithmetic { i -> i / b.toDouble() }
    } else {
        this.numberArithmetic({ i -> i / b.toLong() }, { i -> i / b.toDouble() })
    }
}

/**
 * Returns a new TomlPrimitive which represents the value of this primitive
 * modulo the given number. If the number is a double or float, the returned primitive
 * will be a float primitive. Otherwise, this returns a primitive of the exact same type.
 */
operator fun TomlPrimitive.rem(b: Number): TomlPrimitive {
    return if (b is Double || b is Float) {
        this.floatArithmetic { i -> i % b.toDouble() }
    } else {
        this.numberArithmetic({ i -> i % b.toLong() }, { i -> i % b.toDouble() })
    }
}

// Array Operators

/**
 * Sets the Nth element in the array
 * after coercing the element into a TomlValue
 * @throws UnsupportedOperationException Value is not coercible
 */
operator fun TomlArray.set(index: Int, value: Any) {
    this.set(index, value.asTomlValue)
}

/**
 * Check if the array contains the given element
 * after trying to coerce it into a TomlValue. If the
 * element is non-coercible, this returns false.
 */
operator fun TomlArray.contains(v: Any): Boolean {
    val tv: TomlValue
    try {
        tv = v.asTomlValue
    } catch (e: UnsupportedOperationException) {
        return false
    }
    return this.contains(tv)
}

// Table Operators

/**
 * Gets the value at the given key, coercing the key
 * into a TomlKey. If the key is already a TomlKey, it is
 * used as-is. Otherwise, it is stringified and parsed.
 */
operator fun TomlTable.get(key: Any?): TomlValue? {
    return this.get(key.asTomlKey(true))
}

/**
 * Sets the value at the given key, coercing the key
 * into a TomlKey and coercing the value into a TomlValue.
 * If the key is already a TomlKey, it is
 * used as-is. Otherwise, it is stringified and parsed.
 * @throws UnsupportedOperationException Value is not coercible
 */
operator fun TomlTable.set(key: Any?, value: Any) {
    this.put(key.asTomlKey(true), value.asTomlValue)
}

/**
 * Sets the value at the given key
 */
operator fun TomlTable.set(key: TomlKey, value: TomlValue) {
    this.put(key, value)
}
