# JToml
The ultimate [TOML](https://toml.io/en/v1.0.0) parser for Java 8+.
Goals are feature-completeness and a robust, type-safe API.
JToml supports the latest version of the TOML spec (``v1.0.0``).
TOML is a first-class citizen; no casts, no wonky date-time handling,
no dependencies.

The main reason for JToml's existence is that there were no other pure-Java TOML
parsers which exposed an opaque API for TOML keys. JToml can parse TOML String keys like the other libraries, but also slice and join keys and perform shallow and deep enumeration of keys.

The strongly typed nature of JToml is both an extension of this goal and a personal preference.

## Quick Start
### Declaration
#### Gradle (Kotlin)
```kotlin
dependencies {
    implementation("io.github.wasabithumb:jtoml:0.1.0")
}
```

#### Gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.wasabithumb:jtoml:0.1.0'
}
```

#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.wasabithumb</groupId>
        <artifactId>jtoml</artifactId>
        <version>0.1.0</version>
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
| ``ReflectTomlSerializer`` | ``jtoml-serializer-reflect`` | ``TomlSerializable``, ``Map<String, ?>``, ``List<?>``, boxed and unboxed primitives

### Example
```java
toml.serialize(JsonObject.class, table);
// {"a":{"number":3405691582,"b":{"string":"hello from jtoml 😎"}}}
```

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