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

package betamax.storage.yaml

import betamax.StorableTape
import java.text.Normalizer
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.error.YAMLException
import betamax.storage.*
import org.yaml.snakeyaml.*
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

class YamlTape extends MemoryTape implements StorableTape {

	void writeTo(Writer writer) {
		yaml.dump(this, writer)
	}

	void readFrom(Reader reader) {
		try {
			// TODO: loading a tape, cloning its state & discarding it feels a little wrong
			def tape = yaml.loadAs(reader, YamlTape)
			this.name = tape.name
			this.interactions = tape.interactions
		} catch (YAMLException e) {
			throw new TapeLoadException("Invalid tape", e)
		}
	}

	String getFilename() {
		def normalizedName = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll(/\p{InCombiningDiacriticalMarks}+/, "").replaceAll(/[^\w\d]+/, "_").replaceFirst(/^_/, "").replaceFirst(/_$/, "")
		"${normalizedName}.yaml"
	}

	private Yaml getYaml() {
		def representer = new TapeRepresenter()
		representer.addClassTag(YamlTape, "!tape")

		def constructor = new Constructor()
		constructor.addTypeDescription(new TypeDescription(YamlTape, "!tape"))

		def dumperOptions = new DumperOptions(defaultFlowStyle: BLOCK)

		new Yaml(constructor, representer, dumperOptions)
	}
}
