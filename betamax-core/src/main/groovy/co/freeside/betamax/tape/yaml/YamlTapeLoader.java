/*
 * Copyright 2011 Rob Fletcher
 *
 * Converted from Groovy to Java by Sean Freitag
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

package co.freeside.betamax.tape.yaml;

import co.freeside.betamax.tape.StorableTape;
import co.freeside.betamax.tape.TapeLoader;
import com.google.common.io.Files;

import java.io.*;
import java.text.Normalizer;
import java.util.logging.Logger;
import java.nio.charset.Charset;

public class YamlTapeLoader implements TapeLoader<YamlTape> {

    public static final String FILE_CHARSET = "UTF-8";
    private final File tapeRoot;
    private static final Logger log = Logger.getLogger(YamlTapeLoader.class.getName());

    public YamlTapeLoader(File tapeRoot) {
        this.tapeRoot = tapeRoot;
    }

    public YamlTape loadTape(String name) {
        File file = fileFor(name);
        if (file.isFile()) {
            try {
                BufferedReader reader = Files.newReader(file, Charset.forName(FILE_CHARSET));
                YamlTape tape = YamlTape.readFrom(reader);
                log.info("loaded tape with " + String.valueOf(tape.size()) + " recorded interactions from file " + file.getName() + "...");
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
                log.info("writing tape " + String.valueOf(tape) + " to file " + file.getName() + "...");
                tape.writeTo(bufferedWriter);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
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
}
