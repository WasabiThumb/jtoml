/*
 * Copyright 2025 Xavier Pedraza
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
import io.github.wasabithumb.jtoml.serial.TomlSerializerService;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

@ApiStatus.Internal
final class JTomlImpl implements JToml {

    private static final List<TomlSerializerFactory> SERIALIZER_FACTORIES;
    static {
        List<TomlSerializerFactory> factories = new LinkedList<>();

        // Legacy support: eventually remove this
        for (TomlSerializerService tomlSerializerService : stubbornServiceLoader(TomlSerializerService.class))
            factories.add(TomlSerializerFactory.of(tomlSerializerService));

        // New serializers
        for (TomlSerializerFactory tomlSerializerFactory : stubbornServiceLoader(TomlSerializerFactory.class))
            factories.add(tomlSerializerFactory);

        SERIALIZER_FACTORIES = Collections.unmodifiableList(new ArrayList<>(factories));
    }

    private static <T> @NotNull ServiceLoader<T> stubbornServiceLoader(@NotNull Class<T> type) {
        ServiceLoader<T> ret = ServiceLoader.load(type);
        if (!ret.iterator().hasNext()) ret = ServiceLoader.load(type, JToml.class.getClassLoader());
        return ret;
    }

    //

    private final JTomlOptions options;

    JTomlImpl(@NotNull JTomlOptions options) {
        this.options = options;
    }

    //

    @Override
    public @NotNull String version() {
        return JTomlVersionInfo.derivedVersion();
    }

    @Override
    public @NotNull JTomlOptions options() {
        return this.options;
    }

    private @NotNull TomlTable read(@NotNull BufferedCharSource cs) throws TomlException {
        TableReader tr = new TableReader(cs, this.options);
        return tr.readTable();
    }

    @Override
    public @NotNull TomlDocument readFromString(@NotNull String toml) throws TomlException {
        try (StringCharSource cs = new StringCharSource(toml)) {
            TomlTable table = this.read(new BufferedCharSource(cs));
            return new TomlDocumentImpl(table);
        }
    }

    @Override
    public @NotNull TomlDocument read(@NotNull InputStream in) throws TomlException {
        StreamCharSource cs = new StreamCharSource(in, this.options.get(JTomlOption.READ_BOM));
        TomlTable table = this.read(new BufferedCharSource(cs));
        TomlDocumentImpl doc = new TomlDocumentImpl(table);
        doc.setOrderMarked(cs.didReadBOM());
        return doc;
    }

    @Override
    public @NotNull TomlDocument read(@NotNull Reader reader) throws TomlException {
        ReaderCharSource cs = new ReaderCharSource(reader, this.options.get(JTomlOption.READ_BOM));
        TomlTable table = this.read(new BufferedCharSource(cs));
        TomlDocumentImpl doc = new TomlDocumentImpl(table);
        doc.setOrderMarked(cs.didReadBOM());
        return doc;
    }

    //

    private void write(@NotNull CharTarget ct, @NotNull TomlTable table) throws TomlException {
        TableWriter tw = new TableWriter(ct, this.options);
        tw.writeTable(table);
    }

    @Override
    public @NotNull String writeToString(@NotNull TomlTable table) {
        try (StringCharTarget ct = new StringCharTarget()) {
            this.write(ct, table);
            return ct.toString();
        }
    }

    @Override
    public void write(@NotNull OutputStream out, @NotNull TomlTable table) throws TomlIOException {
        WriterCharTarget ct = WriterCharTarget.of(out);
        if (this.shouldWriteBOM(table)) ct.put(0xFEFF);
        this.write((CharTarget) ct, table);
        ct.flush();
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull TomlTable table) throws TomlIOException {
        WriterCharTarget ct = new WriterCharTarget(writer);
        if (this.shouldWriteBOM(table)) ct.put(0xFEFF);
        this.write((CharTarget) ct, table);
        ct.flush();
    }

    //

    @Override
    public <T> @NotNull T fromToml(@NotNull Class<T> type, @NotNull TomlTable table) throws IllegalArgumentException {
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
    public @NotNull <T> TomlTable toToml(@NotNull Class<T> type, @NotNull T data) throws IllegalArgumentException {
        return this.toTomlUnsafe(type, data);
    }

    @Override
    public @NotNull TomlTable toToml(@NotNull Object data) throws IllegalArgumentException {
        return this.toTomlUnsafe(data.getClass(), data);
    }

    private <T> @NotNull TomlTable toTomlUnsafe(@NotNull Class<T> type, @NotNull Object data) throws IllegalArgumentException {
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

    private boolean shouldWriteBOM(@NotNull TomlTable table) {
        OrderMarkPolicy policy = this.options.get(JTomlOption.WRITE_BOM);
        if (policy == OrderMarkPolicy.NEVER) return false;
        return policy == OrderMarkPolicy.ALWAYS ||
                (policy == OrderMarkPolicy.IF_PRESENT && table instanceof TomlDocument &&
                        ((TomlDocumentImpl) table).isOrderMarked());
    }

}
