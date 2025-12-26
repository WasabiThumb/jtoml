package io.github.wasabithumb.jtoml.serial.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.serial.TomlSerializer;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Serializer leveraging the Gson API to convert
 * TOML tables to/from JSON objects.
 * @see #instance()
 * @see #fromToml(TomlTable)
 * @see #toToml(JsonObject)
 */
public final class GsonTomlSerializer implements TomlSerializer.Symmetric<JsonObject> {

    private static final GsonTomlSerializer DEFAULT_INSTANCE = new GsonTomlSerializer();

    /**
     * Provides the global singleton
     * instance of {@link GsonTomlSerializer}.
     */
    @Contract(pure = true)
    public static @NotNull GsonTomlSerializer instance() {
        return DEFAULT_INSTANCE;
    }

    //

    private GsonTomlSerializer() { }

    //

    @Override
    public @NotNull Class<JsonObject> serialType() {
        return JsonObject.class;
    }

    @Override
    public @NotNull JsonObject fromToml(@NotNull TomlTable table) {
        JsonObject ret = new JsonObject();
        TomlValue value;
        for (TomlKey key : table.keys(false)) {
            value = table.get(key);
            assert value != null;
            ret.add(key.get(0), this.serializeValue(value));
        }
        return ret;
    }

    @Override
    public @NotNull TomlTable toToml(@NotNull JsonObject data) {
        TomlTable ret = TomlTable.create();
        String key;
        JsonElement value;
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            ret.put(TomlKey.literal(key), this.deserializeElement(value));
        }
        return ret;
    }

    @Override
    public @NotNull JsonObject serialize(@NotNull TomlTable table) {
        return this.fromToml(table);
    }

    @Override
    public @NotNull TomlTable deserialize(@NotNull JsonObject data) {
        return this.toToml(data);
    }

    //

    private @NotNull JsonElement serializeValue(@NotNull TomlValue value) {
        if (value.isTable()) {
            return this.fromToml(value.asTable());
        } else if (value.isArray()) {
            TomlArray tomlArray = value.asArray();
            JsonArray array = new JsonArray(tomlArray.size());
            for (TomlValue val : tomlArray) array.add(this.serializeValue(val));
            return array;
        } else {
            return this.serializePrimitive(value.asPrimitive());
        }
    }

    private @NotNull JsonElement serializePrimitive(@NotNull TomlPrimitive v) {
        if (v.isBoolean()) {
            return new JsonPrimitive(v.asBoolean());
        } else if (v.isInteger()) {
            return new JsonPrimitive(v.asLong());
        } else if (v.isFloat()) {
            return new JsonPrimitive(v.asDouble());
        } else  {
            return new JsonPrimitive(v.asString());
        }
    }

    private @NotNull TomlValue deserializeElement(@NotNull JsonElement value) {
        if (value.isJsonNull()) {
            return TomlPrimitive.of("null");
        } else if (value.isJsonArray()) {
            JsonArray jsonArray = value.getAsJsonArray();
            TomlArray array = TomlArray.create(jsonArray.size());
            for (JsonElement el : jsonArray) array.add(this.deserializeElement(el));
            return array;
        } else if (value.isJsonObject()) {
            return this.toToml(value.getAsJsonObject());
        } else {
            return this.deserializePrimitive(value.getAsJsonPrimitive());
        }
    }

    private @NotNull TomlPrimitive deserializePrimitive(@NotNull JsonPrimitive v) {
        if (v.isBoolean()) {
            return TomlPrimitive.of(v.getAsBoolean());
        } else if (v.isString()) {
            return TomlPrimitive.of(v.getAsString());
        } else if (v.isNumber()) {
            Number n = v.getAsNumber();
            if (n instanceof Long) {
                return TomlPrimitive.of(n.longValue());
            } else if (n instanceof Integer) {
                return TomlPrimitive.of(n.intValue());
            } else if (n instanceof Float) {
                return TomlPrimitive.of(n.floatValue());
            } else {
                return TomlPrimitive.of(n.doubleValue());
            }
        } else {
            return TomlPrimitive.of(v.getAsString());
        }
    }

}
