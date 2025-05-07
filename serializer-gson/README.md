# JToml Gson Serializer
Serializes TOML tables to/from
[JsonObject](https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/JsonObject.html)

**Artifact ID**: ``jtoml-serializer-gson``

## Example
```java
JToml toml = JToml.jToml();
TomlTable table = toml.readFromString("[a]\nb = 'c'");
JsonObject json = toml.serialize(JsonObject.class, table);
System.out.println(object); // {"a":{"b":"c"}}
```
