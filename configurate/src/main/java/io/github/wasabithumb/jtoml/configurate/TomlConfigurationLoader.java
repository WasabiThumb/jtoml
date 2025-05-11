package io.github.wasabithumb.jtoml.configurate;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import net.kyori.option.OptionSchema;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.BufferedReader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@DefaultQualifier(NonNull.class)
public final class TomlConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(
            String.class, Boolean.class, Integer.class, Float.class, OffsetDateTime.class, LocalDateTime.class,
            LocalDate.class, LocalTime.class);
    //private static final TypeSerializerCollection GSON_SERIALIZERS = TypeSerializerCollection.defaults().childBuilder()
    //        .register(JsonElement.class, JsonElementSerializer.INSTANCE)
    //        .build();

    // visible for tests
    static final ConfigurationOptions DEFAULT_OPTIONS = ConfigurationOptions.defaults()
            .nativeTypes(NATIVE_TYPES)
            ;//.serializers(GSON_SERIALIZERS);
    private final JToml jtoml;

    TomlConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.HASH});
        this.jtoml = JToml.jToml();
    }

    @Override
    protected void checkCanWrite(final ConfigurationNode node) throws ConfigurateException {
        if (!node.isMap()) {
            throw new ConfigurateException(node, "The root node must be a map in order to save as TOML");
        }
    }

    @Override
    protected void loadInternal(final BasicConfigurationNode node, final BufferedReader reader) throws ParsingException {
        final TomlDocument tomlDocument = this.jtoml.read(reader);
        // TODO: catch TomlParseException / TomlIOException (?)
        populateNode(node, tomlDocument);
    }

    private static void populateNode(final ConfigurationNode node, final TomlTable tomlTable) throws ParsingException {
        for (final TomlKey key : tomlTable.keys(false)) {
            final @Nullable TomlValue value = tomlTable.get(key);
            if (value == null) {
                continue;
            }
            final ConfigurationNode child = node.node(key.get(0));
            populateNode(child, value);
        }
    }

    private static void populateNode(final ConfigurationNode node, final TomlValue value) throws ParsingException {
        if (value.isTable()) {
            node.raw(new HashMap<>());
            populateNode(node, value.asTable());
        } else if (value.isArray()) {
            node.raw(new ArrayList<>());
            for (final TomlValue arrayValue : value.asArray()) {
                final ConfigurationNode child = node.appendListNode();
                populateNode(child, arrayValue);
            }
        } else if (value.isPrimitive()) {
            final TomlPrimitive primitive = value.asPrimitive();
            switch (primitive.type()) {
                case STRING:
                    node.raw(primitive.asString());
                    break;
                case BOOLEAN:
                    node.raw(primitive.asBoolean());
                    break;
                case INTEGER:
                    node.raw(primitive.asInteger());
                    break;
                case FLOAT:
                    node.raw(primitive.asDouble());
                    break;
                case OFFSET_DATE_TIME:
                    node.raw(primitive.asOffsetDateTime());
                    break;
                case LOCAL_DATE_TIME:
                    node.raw(primitive.asLocalDateTime());
                    break;
                case LOCAL_DATE:
                    node.raw(primitive.asLocalDate());
                    break;
                case LOCAL_TIME:
                    node.raw(primitive.asLocalTime());
                    break;
            }
        }
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) throws ConfigurateException {
        final TomlTable document = TomlTable.create();
        populateTable(document, node);
        this.jtoml.write(writer, document);
        // TODO: Catch TomlIOException (?)
    }

    private static void populateTable(final TomlTable table, final ConfigurationNode node) throws ConfigurateException {
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
            final Object key = entry.getKey();
            final ConfigurationNode child = entry.getValue();
            if (child.virtual()) {
                continue;
            }
            final TomlValue v = makeValue(child);
            final TomlKey tomlKey = TomlKey.literal(key.toString());
            table.put(tomlKey, v);
        }
    }

    private static @NotNull TomlValue makeValue(final ConfigurationNode child) throws ConfigurateException {
        if (child.isMap()) {
            final TomlTable childTable = TomlTable.create();
            populateTable(childTable, child);
            return childTable;
        } else if (child.isList()) {
            final TomlArray arr = TomlArray.create();
            for (final ConfigurationNode listChild : child.childrenList()) {
                if (listChild.virtual()) {
                    continue;
                }
                final TomlValue value = makeValue(listChild);
                arr.add(value);
            }
            return arr;
        } else {
            final @Nullable Object value = child.raw();
            if (value instanceof String) {
                return TomlPrimitive.of((String) value);
            } else if (value instanceof Boolean) {
                return TomlPrimitive.of((Boolean) value);
            } else if (value instanceof Integer) {
                return TomlPrimitive.of((Integer) value);
            } else if (value instanceof Float) {
                return TomlPrimitive.of((Float) value);
            } else if (value instanceof OffsetDateTime) {
                return TomlPrimitive.of((OffsetDateTime) value);
            } else if (value instanceof LocalDateTime) {
                return TomlPrimitive.of((LocalDateTime) value);
            } else if (value instanceof LocalDate) {
                return TomlPrimitive.of((LocalDate) value);
            } else if (value instanceof LocalTime) {
                return TomlPrimitive.of((LocalTime) value);
            }
            throw new ConfigurateException("Unsupported type: " + value);
        }
    }

    @Override
    public BasicConfigurationNode createNode(final ConfigurationOptions options) {
        return BasicConfigurationNode.root(options.nativeTypes(NATIVE_TYPES));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractConfigurationLoader.Builder<TomlConfigurationLoader.Builder, TomlConfigurationLoader> {

        private static final OptionSchema.Mutable UNSAFE_SCHEMA = OptionSchema.childSchema(AbstractConfigurationLoader.Builder.SCHEMA);

        public static final OptionSchema SCHEMA = UNSAFE_SCHEMA.frozenView();

        Builder() {
            this.defaultOptions(DEFAULT_OPTIONS);
        }

        @Override
        protected OptionSchema optionSchema() {
            return SCHEMA;
        }

        @Override
        public TomlConfigurationLoader build() {
            this.defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            return new TomlConfigurationLoader(this);
        }
    }
}
