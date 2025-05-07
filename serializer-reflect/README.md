# JToml Reflect Serializer
Serializes TOML tables to/from Java classes

**Artifact ID**: ``jtoml-serializer-reflect``

## Usage
```java
class InnerTestClass implements TomlSerializable {
    
    String bar;
    
}

class TestClass implements TomlSerializable {
    
    InnerTestClass foo;
    
}
```
```java
String src = "[foo] \n bar = 'baz'";
TestClass data = toml.deserialize(TestClass.class, src);
System.out.println(data.foo.bar); // baz
```
