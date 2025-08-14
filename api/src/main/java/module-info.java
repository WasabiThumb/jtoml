/**
 * Defines the JToml API.
 */
module io.github.wasabithumb.jtoml {
    requires org.jetbrains.annotations;

    exports io.github.wasabithumb.jtoml;
    exports io.github.wasabithumb.jtoml.comment;
    exports io.github.wasabithumb.jtoml.document;
    exports io.github.wasabithumb.jtoml.except;
    exports io.github.wasabithumb.jtoml.except.parse;
    exports io.github.wasabithumb.jtoml.key;
    exports io.github.wasabithumb.jtoml.option;
    exports io.github.wasabithumb.jtoml.option.prop;
    exports io.github.wasabithumb.jtoml.serial;
    exports io.github.wasabithumb.jtoml.serial.plain;
    exports io.github.wasabithumb.jtoml.value;
    exports io.github.wasabithumb.jtoml.value.array;
    exports io.github.wasabithumb.jtoml.value.primitive;
    exports io.github.wasabithumb.jtoml.value.table;

    // Hooks into the impl via serice loading
    requires static io.github.wasabithumb.jtoml.impl;
    uses io.github.wasabithumb.jtoml.impl.JTomlServiceImpl;
}
