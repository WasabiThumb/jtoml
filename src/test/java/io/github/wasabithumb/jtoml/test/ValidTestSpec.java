package io.github.wasabithumb.jtoml.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class ValidTestSpec extends AbstractTestSpec {

    private static final Gson GSON = new Gson();

    //

    private final boolean hasValidator;

    public ValidTestSpec(String name, boolean hasValidator) {
        super(name);
        this.hasValidator = hasValidator;
    }

    //

    @Override
    public boolean shouldFail() {
        return false;
    }

    @Override
    public void validate(TomlTable table) throws IOException {
        if (!this.hasValidator) return;
        JsonObject validator = this.readValidator();
        this.validateTable(null, table, validator);
    }

    private void validateTable(String descriptor, TomlTable table, JsonObject validator) {
        String specifier;
        if (descriptor == null) {
            specifier = "Root table";
        } else {
            specifier = "Table \"" + descriptor + "\"";
        }

        Set<String> extraKeys = new HashSet<>();
        for (TomlKey tk : table.keys(false)) extraKeys.add(tk.get(0));

        TomlValue tv;
        JsonElement jv;
        for (String vk : validator.keySet()) {
            TomlKey tk = TomlKey.literal(vk);
            tv = table.get(tk);
            jv = validator.get(vk);
            assertNotNull(tv, specifier + " is missing key \"" + vk + "\"");
            extraKeys.remove(vk);
            this.validateAny(
                    (descriptor == null) ? tk.toString() : descriptor + "." + tk,
                    tv,
                    jv
            );
        }

        assertTrue(
                extraKeys.isEmpty(),
                () -> specifier + " has extra keys: " + String.join(", ", extraKeys)
        );
    }

    private void validateAny(String descriptor, TomlValue value, JsonElement validator) {
        if (validator.isJsonObject()) {
            JsonObject jo = validator.getAsJsonObject();
            JsonElement pt = jo.get("type");
            JsonElement pv = jo.get("value");

            if (pt != null && pv != null) {
                this.validatePrimitive(descriptor, value, pt.getAsString(), pv.getAsString());
                return;
            }

            assertTrue(value.isTable(), "Expected " + descriptor + " to be a table");
            this.validateTable(descriptor, value.asTable(), jo);
        } else {
            assertTrue(validator.isJsonArray());
            JsonArray ja = validator.getAsJsonArray();
            assertTrue(value.isArray(), "Expected " + descriptor + " to be an array");
            TomlArray ta = value.asArray();
            assertEquals(ja.size(), ta.size(), descriptor + " has incorrect array length");
            for (int i=0; i < ta.size(); i++) {
                this.validateAny(descriptor + "[" + i + "]", ta.get(i), ja.get(i));
            }
        }
    }

    private void validatePrimitive(String descriptor, TomlValue value, String vType, String vValue) {
        assertTrue(value.isPrimitive(), "Expected " + descriptor + " to be a primitive");
        TomlPrimitive primitive = value.asPrimitive();

        if ("string".equals(vType)) {
            assertTrue(primitive.isString(), "Expected " + descriptor + " to be a string");
        } else if ("integer".equals(vType)) {
            assertTrue(primitive.isInteger(), "Expected " + descriptor + " to be an integer");
        } else if ("float".equals(vType)) {
            assertTrue(primitive.isFloat(), "Expected " + descriptor + " to be a float");
        } else if ("bool".equals(vType)) {
            assertTrue(primitive.isBoolean(), "Expected " + descriptor + " to be a boolean");
        } else if ("datetime".equals(vType)) {
            assertTrue(primitive.isOffsetDateTime(), "Expected " + descriptor + " to be an offset date-time");
        } else if ("datetime-local".equals(vType)) {
            assertTrue(primitive.isLocalDateTime(), "Expected " + descriptor + " to be a local date-time");
        } else if ("date-local".equals(vType)) {
            assertTrue(primitive.isLocalDate(), "Expected " + descriptor + " to be a local date");
        } else {
            assertEquals("time-local", vType);
            assertTrue(primitive.isLocalTime(), "Expected " + descriptor + " tto be a local time");
        }

        assertEquals(vValue, primitive.asString());
    }

    private JsonObject readValidator() throws IOException {
        try (InputStream is = this.open("/tests/" + this.name + ".json");
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             JsonReader jr = new JsonReader(isr)
        ) {
            return GSON.fromJson(jr, JsonObject.class);
        }
    }

}
