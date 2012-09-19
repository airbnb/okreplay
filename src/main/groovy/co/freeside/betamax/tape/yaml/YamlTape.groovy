/*
 * Copyright 2011 Rob Fletcher
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

package co.freeside.betamax.tape.yaml

import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import co.freeside.betamax.tape.MemoryTape
import co.freeside.betamax.tape.StorableTape
import co.freeside.betamax.tape.TapeLoadException
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.error.YAMLException
import org.yaml.snakeyaml.nodes.Tag

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

class YamlTape extends MemoryTape implements StorableTape {

	public static final Tag TAPE_TAG = new Tag('!tape')

    private boolean dirty = false

    static YamlTape readFrom(Reader reader) {
        try {
            yaml.loadAs(reader, YamlTape)
        } catch (YAMLException e) {
            throw new TapeLoadException('Invalid tape', e)
        }
    }

    void writeTo(Writer writer) {
        yaml.dump(this, writer)
    }

    boolean isDirty() {
        dirty
    }

    @Override
    void record(Request request, Response response) {
        super.record(request, response)
        dirty = true
    }

    private static Yaml getYaml() {
        def representer = new TapeRepresenter()
		representer.addClassTag(YamlTape, TAPE_TAG)

        def constructor = new Constructor()
        constructor.addTypeDescription(new TypeDescription(YamlTape, TAPE_TAG))

        def dumperOptions = new DumperOptions(defaultFlowStyle: BLOCK, width: 256)

        new Yaml(constructor, representer, dumperOptions)
    }
}
