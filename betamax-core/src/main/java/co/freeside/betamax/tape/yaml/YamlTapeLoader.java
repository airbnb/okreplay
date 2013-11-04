/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.tape.yaml;

import java.io.*;
import java.nio.charset.*;
import java.text.*;
import java.util.logging.*;
import co.freeside.betamax.tape.*;
import com.google.common.annotations.*;
import com.google.common.io.*;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.*;
import org.yaml.snakeyaml.error.*;

public class YamlTapeLoader implements TapeLoader<YamlTape> {

    public static final String FILE_CHARSET = "UTF-8";

    private final File tapeRoot;

    private static final Logger LOG = Logger.getLogger(YamlTapeLoader.class.getName());

    public YamlTapeLoader(File tapeRoot) {
        this.tapeRoot = tapeRoot;
    }

    public YamlTape loadTape(String name) {
        File file = fileFor(name);
        if (file.isFile()) {
            try {
                BufferedReader reader = Files.newReader(file, Charset.forName(FILE_CHARSET));
                YamlTape tape = readFrom(reader);
                LOG.info(String.format("loaded tape with %d recorded interactions from file %s...", tape.size(), file.getAbsolutePath()));
                return tape;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            YamlTape tape = new YamlTape();
            tape.setName(name);
            return tape;
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void writeTape(final StorableTape tape) {
        File file = fileFor(tape.getName());
        file.getParentFile().mkdirs();
        if (tape.isDirty()) {
            try {
                BufferedWriter bufferedWriter = Files.newWriter(file, Charset.forName(FILE_CHARSET));
                LOG.info(String.format("writing tape %s to file %s...", tape.getName(), file.getAbsolutePath()));
                writeTo(tape, bufferedWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @VisibleForTesting
    public YamlTape readFrom(Reader reader) {
        try {
            return getYaml().loadAs(reader, YamlTape.class);
        } catch (YAMLException e) {
            throw new TapeLoadException("Invalid tape", e);
        }
    }

    @VisibleForTesting
    public void writeTo(StorableTape tape, Writer writer) throws IOException {
        try {
            getYaml().dump(tape, writer);
        } finally {
            writer.close();
        }
    }

    public File fileFor(String tapeName) {
        final String normalizedName = Normalizer.normalize(tapeName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^\\w\\d]+", "_")
                .replaceFirst("^_", "")
                .replaceFirst("_$", "");
        return new File(tapeRoot, normalizedName + ".yaml");
    }

    public final File getTapeRoot() {
        return tapeRoot;
    }

    private Yaml getYaml() {
        TapeRepresenter representer = new TapeRepresenter();
        representer.addClassTag(YamlTape.class, YamlTape.TAPE_TAG);

        Constructor constructor = new TapeConstructor();

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setWidth(256);

        return new Yaml(constructor, representer, dumperOptions);
    }

}
