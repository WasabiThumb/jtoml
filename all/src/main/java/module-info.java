module io.github.wasabithumb.jtoml {
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
    exports io.github.wasabithumb.jtoml.serial.gson;
    exports io.github.wasabithumb.jtoml.serial.reflect;
    exports io.github.wasabithumb.jtoml.value;
    exports io.github.wasabithumb.jtoml.value.array;
    exports io.github.wasabithumb.jtoml.value.primitive;
    exports io.github.wasabithumb.jtoml.value.table;
    exports io.github.wasabithumb.jtoml.configurate;

    requires static org.jetbrains.annotations;
    requires static com.google.gson;
    requires static org.spongepowered.configurate;
    uses org.spongepowered.configurate.loader.ConfigurationFormat;
}