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

import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.error.YAMLException

import org.yaml.snakeyaml.*
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
import co.freeside.betamax.tape.MemoryTape
import co.freeside.betamax.proxy.Request
import co.freeside.betamax.proxy.Response
import co.freeside.betamax.tape.StorableTape
import co.freeside.betamax.tape.TapeLoadException

class YamlTape extends MemoryTape implements StorableTape {

    private boolean dirty = false

    static YamlTape readFrom(Reader reader) {
        try {
            yaml.loadAs(reader, YamlTape)
        } catch (YAMLException e) {
            throw new TapeLoadException("Invalid tape", e)
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
        representer.addClassTag(YamlTape, "!tape")

        def constructor = new Constructor()
        constructor.addTypeDescription(new TypeDescription(YamlTape, "!tape"))

        def dumperOptions = new DumperOptions(defaultFlowStyle: BLOCK, width: 256)

        new Yaml(constructor, representer, dumperOptions)
    }
}
