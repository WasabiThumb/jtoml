/*
 * Copyright 2026 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    public static GsonTomlSerializer instance() {
        return DEFAULT_INSTANCE;
    }

    //

    private GsonTomlSerializer() { }

    //

    @Override
    public Class<JsonObject> serialType() {
        return JsonObject.class;
    }

    @Override
    public JsonObject fromToml(TomlTable table) {
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
    public TomlTable toToml(JsonObject data) {
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

    //

    private JsonElement serializeValue(TomlValue value) {
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

    private JsonElement serializePrimitive(TomlPrimitive v) {
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

    private TomlValue deserializeElement(JsonElement value) {
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

    private TomlPrimitive deserializePrimitive(JsonPrimitive v) {
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
