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
import java.nio.charset.Charset;
import java.util.logging.Logger;
import co.freeside.betamax.io.*;
import co.freeside.betamax.tape.*;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.representer.Representer;
import static co.freeside.betamax.tape.yaml.YamlTape.TAPE_TAG;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

public class YamlTapeLoader implements TapeLoader<YamlTape> {

    public static final String FILE_CHARSET = "UTF-8";

    private final FileResolver fileResolver;

    private static final Logger LOG = Logger.getLogger(YamlTapeLoader.class.getName());

    public YamlTapeLoader(File tapeRoot) {
        fileResolver = new FileResolver(tapeRoot);
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
            return newTape(name);
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void writeTape(final Tape tape) {
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
    public YamlTape newTape(String name) {
        YamlTape tape = new YamlTape(fileResolver);
        tape.setName(name);
        return tape;
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
    public void writeTo(Tape tape, Writer writer) throws IOException {
        try {
            getYaml().dump(tape, writer);
        } finally {
            writer.close();
        }
    }

    public File fileFor(String tapeName) {
        final String normalizedName = FilenameNormalizer.toFilename(tapeName);
        return fileResolver.toFile(normalizedName + ".yaml");
    }

    private Yaml getYaml() {
        Representer representer = new TapeRepresenter(fileResolver);
        representer.addClassTag(YamlTape.class, TAPE_TAG);

        Constructor constructor = new TapeConstructor(fileResolver);
        constructor.addTypeDescription(new TypeDescription(YamlTape.class, TAPE_TAG));

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(BLOCK);
        dumperOptions.setWidth(256);

        Yaml yaml = new Yaml(constructor, representer, dumperOptions);
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml;
    }

}
