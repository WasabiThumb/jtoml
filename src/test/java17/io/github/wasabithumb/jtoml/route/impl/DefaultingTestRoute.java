package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.serial.TomlSerializable;
import io.github.wasabithumb.jtoml.serial.reflect.Defaulting;

import static org.junit.jupiter.api.Assertions.*;

public final class DefaultingTestRoute implements TestRoute {

    @Sentinel("defaulting.toml")
    private RecordDocument recordDocument;

    @Sentinel("defaulting.toml")
    private PojoDocument pojoDocument;

    //

    @Override
    public String displayName() {
        return "Defaulting";
    }

    @Override
    public void execute(JToml instance) {
        checkDocument(this.recordDocument);
        checkDocument(this.pojoDocument);
    }

    private void checkDocument(Document document) {
        assertEquals(0xCAFEB015L, document.sanity());
        assertNull(document.someString());
        assertFalse(document.someBool());
        assertEquals(0d, document.someFloat());
        assertEquals(0, document.someInt());
    }

    //

    private interface Document {
        long sanity();
        String someString();
        boolean someBool();
        double someFloat();
        int someInt();
    }

    @Defaulting
    private record RecordDocument(
            long sanity,
            String someString,
            boolean someBool,
            double someFloat,
            int someInt
    ) implements Document { }

    @Defaulting
    private static final class PojoDocument implements TomlSerializable, Document {

        private long sanity;
        private String someString;
        private boolean someBool;
        private double someFloat;
        private int someInt;

        //

        @Override
        public long sanity() {
            return this.sanity;
        }

        @Override
        public String someString() {
            return this.someString;
        }

        @Override
        public boolean someBool() {
            return this.someBool;
        }

        @Override
        public double someFloat() {
            return this.someFloat;
        }

        @Override
        public int someInt() {
            return this.someInt;
        }

    }

}
