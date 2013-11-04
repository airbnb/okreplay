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
import co.freeside.betamax.message.*;
import co.freeside.betamax.message.tape.*;
import co.freeside.betamax.tape.*;
import com.google.common.io.*;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.*;
import org.yaml.snakeyaml.error.*;
import org.yaml.snakeyaml.nodes.*;

class YamlTape extends MemoryTape implements StorableTape {

    public static final Tag TAPE_TAG = new Tag("!tape");

    private final File tapeRoot;
    private boolean dirty;

    public YamlTape(File tapeRoot) {
        this.tapeRoot = tapeRoot;
    }

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

    protected void writeBodyToExternal(Response response, RecordedResponse clone) throws IOException {
        File body = new File(tapeRoot, "body.txt");
        ByteStreams.copy(response.getBodyAsBinary(), Files.newOutputStreamSupplier(body));
        clone.setBody(body);
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
