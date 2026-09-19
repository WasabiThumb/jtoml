package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Created to cover #76 */
public final class TrickyFloatsTestRoute implements TestRoute {

    @Override
    public String displayName() {
        return "Tricky Floats";
    }

    @Override
    public void execute(JToml instance) {
        // Test 8192 stable random doubles
        Random random = new Random(3277808293637613256L);
        for (int i = 0; i < 8192; i++) {
            double value = Double.longBitsToDouble(random.nextLong());
            System.out.println(value);
            test(instance, value);
        }
    }

    private void test(JToml instance, double value) {
        String doc = "a = " + value + "\n";
        TomlTable table = instance.readFromString(doc);
        TomlValue v = table.get("a");
        assertNotNull(v);
        double out = v.asPrimitive().asDouble();
        double expected = Double.parseDouble(Double.toString(value));
        assertEquals(expected, out);
    }

}
