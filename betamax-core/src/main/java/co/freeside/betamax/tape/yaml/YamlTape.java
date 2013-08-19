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

import co.freeside.betamax.message.Request;
import co.freeside.betamax.message.Response;
import co.freeside.betamax.tape.MemoryTape;
import co.freeside.betamax.tape.StorableTape;
import co.freeside.betamax.tape.TapeLoadException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.Reader;
import java.io.Writer;

public class YamlTape extends MemoryTape implements StorableTape {

    public static final Tag TAPE_TAG = new Tag("!tape");

    private boolean dirty = false;

    public static YamlTape readFrom(Reader reader) {
        try {
            return getYaml().loadAs(reader, YamlTape.class);
        } catch (YAMLException e) {
            throw new TapeLoadException("Invalid tape", e);
        }

    }

    public void writeTo(Writer writer) {
        getYaml().dump(this, writer);
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void record(Request request, Response response) {
        super.record(request, response);
        dirty = true;
    }

    private static Yaml getYaml() {
        TapeRepresenter representer = new TapeRepresenter();
        representer.addClassTag(YamlTape.class, TAPE_TAG);

        Constructor constructor = new Constructor();
        constructor.addTypeDescription(new TypeDescription(YamlTape.class, TAPE_TAG));

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setWidth(256);

        return new Yaml(constructor, representer, dumperOptions);
    }
}
