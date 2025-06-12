# JToml
The ultimate [TOML](https://toml.io/en/v1.0.0) parser for Java 8+.
Goals are feature-completeness and a robust, type-safe API.
JToml supports the latest version of the TOML spec (``v1.0.0``).
TOML is a first-class citizen; no casts, no wonky date-time handling,
no dependencies.

The main reason for JToml's existence is that there were no other pure-Java TOML
parsers which exposed an opaque API for TOML keys. JToml can parse TOML String keys like the other libraries, but also slice and join keys and perform shallow and deep enumeration of keys.

The strongly typed nature of JToml is both an extension of this goal and a personal preference.

## Table of Contents
- [Quick Start](#quick-start)
- [Serializers](#serializers)
- [Configurate](#configurate)
- [Kotlin Extensions](#kotlin-extensions)
- [Comments](#comments)
- [Feature Matrix](#comparison)
- [License](#license)

## Quick Start
### Declaration
#### Gradle (Kotlin)
```kotlin
dependencies {
    implementation("io.github.wasabithumb:jtoml:0.6.0")
}
```

#### Gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.wasabithumb:jtoml:0.6.0'
}
```

#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.wasabithumb</groupId>
        <artifactId>jtoml</artifactId>
        <version>0.6.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Basic Usage
```java
final String source = """
[a]
number = 0xCAFEBABE

[a.b]
string = "hello from jtoml \\U0001F60E"
""";

JToml toml = JToml.jToml();
TomlTable table = toml.readFromString(source);

String key1 = "a.number";
table.get(key1).asPrimitive().asLong();   // 3405691582

TomlKey key2 = TomlKey.join(TomlKey.parse("a.b"), TomlKey.literal("string"));
table.get(key2).asPrimitive().asString(); // hello from jtoml 😎
```
Files and streams are also supported. For streams, very minimal buffering is
performed.

## Serializers
The ``JToml#serialize`` and ``JToml#deserialize`` methods can be used to
convert ``TomlTable``s into various formats. Here is the current list of
serializers:

| Name | Dependency | Types |
| :-: | :-: | :-: |
| ``PlainTextTomlSerializer`` | - included - | ``String`` |
| ``GsonTomlSerializer`` | ``jtoml-serializer-gson`` | [``JsonObject``](https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/JsonObject.html) |
| ``ReflectTomlSerializer`` | ``jtoml-serializer-reflect`` | ``TomlSerializable``, ``Map<String, ?>``, ``List<?>``, records, boxed and unboxed primitives

### Example
```java
toml.serialize(JsonObject.class, table);
// {"a":{"number":3405691582,"b":{"string":"hello from jtoml 😎"}}}
```

### Shading notice
If you are shading this library, serializers will not work as intended unless
you configure shadowJar to merge the service files. See instructions below.

#### Gradle
```kotlin
tasks.shadowJar {
    mergeServiceFiles()
}
```

#### Maven
Use the [ServicesResourceTransformer](https://maven.apache.org/plugins/maven-shade-plugin/examples/resource-transformers.html#ServicesResourceTransformer).

## Configurate
JToml can now be used as a [Configurate](https://github.com/SpongePowered/Configurate) format/loader through the ``jtoml-configurate`` artifact. Special thanks to [jpenilla](https://github.com/jpenilla) for this contribution.

### Examples
```java
ConfigurationFormat format = ConfigurationFormat.forExtension("toml");
ConfigurationNode node = format.create(this.getClass().getResource("foo.toml")).load();
```
```java
TomlConfigurationLoader loader = TomlConfigurationLoader.builder()
        .path(target)
        .set(JTomlOption.LINE_SEPARATOR, LineSeparator.LF)
        .build();

loader.save(node);
```

## Kotlin Extensions
The ``jtoml-kotlin`` artifact provides extensions for Kotlin. Notably adds ``KToml``, a
static instance of JToml. Also adds many extra functions for coercing values, performing primitive arithmetic, and
working with arrays/tables.

### Example
```kotlin
val table = TomlTable.create()
table["foo.bar"] = "baz"
table["meaning.of.life"] = 40.asTomlPrimitive + 2
val str = KToml.writeToString(table)
```

## Comments
By default, JToml will read/write comments to/from documents. Comments can be read & updated
with the ``TomlValue#comments()`` accessor. Comments can be placed in one of 3 "sections";
``PRE``, ``INLINE`` and ``POST``. The ``INLINE`` section is exclusive and can only contain up to 1
comment.

### Integration with ``jtoml-serializer-reflect``
The ``@Comment.Pre``, ``@Comment.Inline`` and ``@Comment.Post`` annotation can be used to add comments
to fields on ``TomlSerializable`` classes or components on records. For instance:
```java
class MyObject implements TomlSerializable {

    @Comment.Inline("my comment")
    String text;

}
```

## Comparison
|                                                             | [WasabiThumb/jtoml](WasabiThumb/jtoml) | [tomlj/tomlj](https://github.com/tomlj/tomlj) | [mwanji/toml4j](https://github.com/mwanji/toml4j) | [asafh/jtoml](https://github.com/asafh/jtoml) |
|:------------------------------------------------------------|:--------------------------------------:|:---------------------------------------------:|:-------------------------------------------------:|:---------------------------------------------:|
| Safe Key Join & Split                                       |                   ✅                    |                       ❌                       |                         ❌                         |                       ❌                       |
| ``v1.0.0`` Compliance                                       |                   ✅                    |                       ✅                       |                         ❌                         |                       ❌                       |
| Positional Errors                                           |                   ✅                    |                       ✅                       |                         ✅                         |                       ✅                       |
| Error Recovery                                              |                   ❌                    |                       ✅                       |                         ✅                         |                       ❌                       |
| Configurable Read Rules                                     |                   ✅                    |                       ✅                       |                         ❌                         |                       ❌                       |
| Configurable Write Rules                                    |                   ✅                    |                       ❌                       |                         ✅                         |                       ❌                       |
| Enum-based type inspection                                  |                   ✅                    |                       ✅                       |                         ❌                         |                       ❌                       |
| Safe type coercion                                          |                   ✅                    |                       ❌                       |                         ❌                         |                       ❌                       |
| Reflect serialization                                       |                   ✅                    |                       ❌                       |                         ✅                         |                       ✅                       |
| JSON serialization                                          |                   ✅                    |                 ✅<sup>1</sup>                 |                         ✅                         |                       ❌                       |
| Zero Dependencies                                           |                   ✅                    |                       ❌                       |                         ❌                         |                       ✅                       |
| Passes [test suite](https://github.com/toml-lang/toml-test) |                   ✅                    |                 ❌<sup>2</sup>                 |                         ❌                         |                       ❌                       |

1. Deserialization is not supported; JSON is in string format
2. Passes [6 arbitrary tests](https://github.com/tomlj/tomlj/tree/e2d94e6dfe7633111b9e5aaec5a71d88c0af94ce/src/test/resources/org/tomlj), full suite has ~700

## License
```text
Copyright 2025 Wasabi Codes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```