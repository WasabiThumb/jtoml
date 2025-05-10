package io.github.wasabithumb.jtoml.test;

import java.io.IOException;
import java.io.InputStream;

abstract class AbstractTestSpec implements TestSpec {

    protected final String name;

    public AbstractTestSpec(String name) {
        this.name = name;
    }

    //

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public InputStream read() throws IOException {
        return this.open("/tests/" + this.name + ".toml");
    }

    protected InputStream open(String path) throws IOException {
        InputStream is = TestSpec.class.getResourceAsStream(path);
        if (is == null) throw new IOException("Resource \"" + path + "\" not found");
        return is;
    }

}
