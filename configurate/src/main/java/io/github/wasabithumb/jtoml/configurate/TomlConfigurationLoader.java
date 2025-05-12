package io.github.wasabithumb.jtoml.configurate;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import io.github.wasabithumb.jtoml.except.parse.TomlLocalParseException;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import net.kyori.option.OptionSchema;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.Contract;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.BufferedReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A loader for TOML-formatted configurations, using the JToml library for
 * parsing and generation.
 */
@DefaultQualifier(NonNull.class)
public final class TomlConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(
            String.class, Boolean.class, Integer.class, Float.class, OffsetDateTime.class, LocalDateTime.class,
            LocalDate.class, LocalTime.class, Number.class);
    private static final TypeSerializerCollection TOML_SERIALIZERS = TypeSerializerCollection.defaults().childBuilder()
            .register(OffsetDateTime.class, new NativeTypeSerializer<>())
            .register(LocalDateTime.class, new NativeTypeSerializer<>())
            .register(LocalDate.class, new NativeTypeSerializer<>())
            .register(LocalTime.class, new NativeTypeSerializer<>())
            // See GsonConfigurationLoader for reference, would allow users to get/set TomlValues to ConfigurationNode
            //.register(TomlValue.class, TomlValueSerializer.INSTANCE)
            .build();

    // visible for tests
    static final ConfigurationOptions DEFAULT_OPTIONS = ConfigurationOptions.defaults()
            .nativeTypes(NATIVE_TYPES)
            .serializers(TOML_SERIALIZERS);

    /**
     * Creates a new {@link TomlConfigurationLoader.Builder}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private final JToml jtoml;

    TomlConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.HASH});
        this.jtoml = JToml.jToml(builder.jTomlOptionsBuilder().build());
    }

    @Override
    protected void checkCanWrite(final ConfigurationNode node) throws ConfigurateException {
        if (!node.isMap()) {
            throw new ConfigurateException(node, "The root node must be a map in order to save as TOML");
        }
    }

    @Override
    protected void loadInternal(final BasicConfigurationNode node, final BufferedReader reader) throws ParsingException {
        try {
            final TomlDocument tomlDocument = this.jtoml.read(reader);
            populateNode(node, tomlDocument);
        } catch (final TomlLocalParseException ex) {
            throw new ParsingException(
                    ex.getLineNumber(),
                    ex.getColumnNumber(),
                    "",
                    "Exception reading TOML document: " + ex.getRawMessage(),
                    ex.getCause()
            );
        } catch (final TomlException ex) {
            throw new ParsingException(
                    ParsingException.UNKNOWN_POS,
                    ParsingException.UNKNOWN_POS,
                    "",
                    "Exception reading TOML document",
                    ex
            );
        }
    }

    private static void populateNode(final ConfigurationNode node, final TomlTable tomlTable) {
        for (final TomlKey key : tomlTable.keys(false)) {
            final @Nullable TomlValue value = tomlTable.get(key);
            if (value == null) {
                continue;
            }
            final ConfigurationNode child = node.node(key.get(0));
            populateNode(child, value);
        }
    }

    private static void populateNode(final ConfigurationNode node, final TomlValue value) {
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
        try {
            this.jtoml.write(writer, document);
        } catch (final TomlIOException ex) {
            throw new ConfigurateException("Exception writing TOML document", ex.getCause());
        }
    }

    private static void populateTable(final TomlTable table, final ConfigurationNode node) throws ConfigurateException {
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
            final Object key = entry.getKey();
            final ConfigurationNode child = entry.getValue();
            if (child.virtual()) {
                continue;
            }
            final TomlValue v = makeValue(child);
            final TomlKey tomlKey = TomlKey.literal(String.valueOf(key));
            table.put(tomlKey, v);
        }
    }

    private static TomlValue makeValue(final ConfigurationNode child) throws ConfigurateException {
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
            } else if (value instanceof Integer || value instanceof Long) {
                return TomlPrimitive.of(((Number) value).longValue());
            } else if (value instanceof Float || value instanceof Double) {
                return TomlPrimitive.of(((Number) value).doubleValue());
            } else if (value instanceof Number) {
                return TomlPrimitive.of(((Number) value).longValue());
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

    /**
     * Builds a {@link TomlConfigurationLoader}.
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<TomlConfigurationLoader.Builder, TomlConfigurationLoader> {

        private static final OptionSchema.Mutable UNSAFE_SCHEMA = OptionSchema.childSchema(AbstractConfigurationLoader.Builder.SCHEMA);

        /**
         * A schema of options available on the TOML loader. Does not include {@link JTomlOption JTomlOptions}.
         */
        public static final OptionSchema SCHEMA = UNSAFE_SCHEMA.frozenView();

        private final JTomlOptions.Builder optionsBuilder;

        Builder() {
            this.defaultOptions(DEFAULT_OPTIONS);
            this.optionsBuilder = JTomlOptions.builder();
        }

        @Override
        protected OptionSchema optionSchema() {
            return SCHEMA;
        }

        /**
         * Returns the {@link JTomlOptions.Builder} used to configure this loader.
         *
         * @return the options builder
         */
        public JTomlOptions.Builder jTomlOptionsBuilder() {
            return this.optionsBuilder;
        }

        @Contract(value = "_, _ -> this", mutates = "this")
        public <T> Builder set(final JTomlOption<T> key, final @Nullable T value) throws IllegalArgumentException {
            this.optionsBuilder.set(key, value);
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public Builder unset(final JTomlOption<?> key) {
            this.optionsBuilder.unset(key);
            return this;
        }

        @Override
        public TomlConfigurationLoader build() {
            this.defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            return new TomlConfigurationLoader(this);
        }
    }

    private static final class NativeTypeSerializer<T> implements TypeSerializer<T> {
        @SuppressWarnings({"DataFlowIssue", "unchecked"})
        @Override
        public T deserialize(final Type type, final ConfigurationNode node) {
            return (T) node.raw();
        }

        @Override
        public void serialize(final Type type, final @Nullable T obj, final ConfigurationNode node) {
            node.raw(obj);
        }
    }
}
