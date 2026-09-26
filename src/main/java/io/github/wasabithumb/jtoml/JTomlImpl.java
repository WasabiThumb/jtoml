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

package io.github.wasabithumb.jtoml;

import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.document.TomlDocumentImpl;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import io.github.wasabithumb.jtoml.io.TableReader;
import io.github.wasabithumb.jtoml.io.TableWriter;
import io.github.wasabithumb.jtoml.io.source.BufferedCharSource;
import io.github.wasabithumb.jtoml.io.source.ReaderCharSource;
import io.github.wasabithumb.jtoml.io.source.StreamCharSource;
import io.github.wasabithumb.jtoml.io.source.StringCharSource;
import io.github.wasabithumb.jtoml.io.target.CharTarget;
import io.github.wasabithumb.jtoml.io.target.StringCharTarget;
import io.github.wasabithumb.jtoml.io.target.WriterCharTarget;
import io.github.wasabithumb.jtoml.meta.JTomlVersionInfo;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.option.prop.OrderMarkPolicy;
import io.github.wasabithumb.jtoml.serial.TomlSerializerFactory;
import io.github.wasabithumb.jtoml.util.Pinned;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.io.*;
import java.util.*;

/**
 * Canonical implementation of
 * {@link JToml}.
 */
@NullMarked
@ApiStatus.Internal
final class JTomlImpl implements JToml {

    private static final List<TomlSerializerFactory> SERIALIZER_FACTORIES;
    static {
        List<TomlSerializerFactory> factories = new ArrayList<>();
        for (TomlSerializerFactory tomlSerializerFactory : stubbornServiceLoader(TomlSerializerFactory.class))
            factories.add(tomlSerializerFactory);
        SERIALIZER_FACTORIES = Collections.unmodifiableList(factories);
    }

    private static <T> ServiceLoader<T> stubbornServiceLoader(Class<T> type) {
        ServiceLoader<T> ret = ServiceLoader.load(type);
        if (!ret.iterator().hasNext()) ret = ServiceLoader.load(type, JToml.class.getClassLoader());
        return ret;
    }

    //

    private final JTomlOptions options;

    @Pinned(reason = "called by JTomlProvider")
    public JTomlImpl(JTomlOptions options) {
        this.options = options;
    }

    //

    @Override
    public String version() {
        return JTomlVersionInfo.derivedVersion();
    }

    @Override
    public JTomlOptions options() {
        return this.options;
    }

    private TomlDocumentImpl read(BufferedCharSource cs) throws TomlException {
        TableReader tr = new TableReader(cs, this.options);
        TomlTable table = tr.readTable();
        return new TomlDocumentImpl(table, tr.issues());
    }

    @Override
    public TomlDocument readFromString(String toml) throws TomlException {
        try (StringCharSource cs = new StringCharSource(toml)) {
            return this.read(new BufferedCharSource(cs));
        }
    }

    @Override
    public TomlDocument read(InputStream in) throws TomlException {
        StreamCharSource cs = new StreamCharSource(in, this.options.get(JTomlOption.READ_BOM));
        TomlDocumentImpl doc = this.read(new BufferedCharSource(cs));
        doc.setOrderMarked(cs.didReadBOM());
        return doc;
    }

    @Override
    public TomlDocument read(Reader reader) throws TomlException {
        ReaderCharSource cs = new ReaderCharSource(reader, this.options.get(JTomlOption.READ_BOM));
        TomlDocumentImpl doc = this.read(new BufferedCharSource(cs));
        doc.setOrderMarked(cs.didReadBOM());
        return doc;
    }

    //

    private void write(CharTarget ct, TomlTable table) throws TomlException {
        TableWriter tw = new TableWriter(ct, this.options);
        tw.writeTable(table);
    }

    @Override
    public String writeToString(TomlTable table) {
        try (StringCharTarget ct = new StringCharTarget()) {
            this.write(ct, table);
            return ct.toString();
        }
    }

    @Override
    public void write(OutputStream out, TomlTable table) throws TomlIOException {
        WriterCharTarget ct = WriterCharTarget.of(out);
        if (this.shouldWriteBOM(table)) ct.put(0xFEFF);
        this.write((CharTarget) ct, table);
        ct.flush();
    }

    @Override
    public void write(Writer writer, TomlTable table) throws TomlIOException {
        WriterCharTarget ct = new WriterCharTarget(writer);
        if (this.shouldWriteBOM(table)) ct.put(0xFEFF);
        this.write((CharTarget) ct, table);
        ct.flush();
    }

    //

    @Override
    public <T> T fromToml(Class<T> type, TomlTable table) throws IllegalArgumentException {
        int count = SERIALIZER_FACTORIES.size();
        String[] issues = new String[count];
        for (int i = 0; i < count; i++) {
            TomlSerializerFactory factory = SERIALIZER_FACTORIES.get(i);
            TomlSerializerFactory.Result<?, T> result = factory.fromToml(this, type);
            if (result.valid()) return result.serializer().fromToml(table);
            issues[i] = result.issue();
        }
        throw new IllegalArgumentException(
                "No serializer found on classpath for type " + type.getName() +
                " (encountered " + count + " issues: " + String.join(", ", issues) + ")"
        );
    }

    @Override
    public <T> TomlTable toToml(Class<T> type, T data) throws IllegalArgumentException {
        return this.toTomlUnsafe(type, data);
    }

    @Override
    public TomlTable toToml(Object data) throws IllegalArgumentException {
        return this.toTomlUnsafe(data.getClass(), data);
    }

    private <T> TomlTable toTomlUnsafe(Class<T> type, Object data) throws IllegalArgumentException {
        int count = SERIALIZER_FACTORIES.size();
        String[] issues = new String[count];
        for (int i = 0; i < count; i++) {
            TomlSerializerFactory factory = SERIALIZER_FACTORIES.get(i);
            TomlSerializerFactory.Result<T, ?> result = factory.toToml(this, type);
            if (result.valid()) return result.serializer().toToml(type.cast(data));
            issues[i] = result.issue();
        }
        throw new IllegalArgumentException(
                "No deserializer found on classpath for type " + type.getName() +
                " (encountered " + count + " issues: " + String.join(", ", issues) + ")"
        );
    }

    //

    private boolean shouldWriteBOM(TomlTable table) {
        OrderMarkPolicy policy = this.options.get(JTomlOption.WRITE_BOM);
        if (policy == OrderMarkPolicy.NEVER) return false;
        return policy == OrderMarkPolicy.ALWAYS ||
                (policy == OrderMarkPolicy.IF_PRESENT && table instanceof TomlDocument &&
                        ((TomlDocumentImpl) table).isOrderMarked());
    }

}
