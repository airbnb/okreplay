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

import groovy.util.logging.Log4j
import org.yaml.snakeyaml.DumperOptions.FlowStyle
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.error.YAMLException
import betamax.storage.*
import org.yaml.snakeyaml.*

@Log4j
class YamlTapeLoader implements TapeLoader {

	/**
	 * Options controlling the style of the YAML written out.
	 */
	DumperOptions dumperOptions = new DumperOptions(defaultFlowStyle: FlowStyle.BLOCK)

	String getFileExtension() {
		"yaml"
	}

	Tape readTape(Reader reader) {
		try {
			def tape = yaml.load(reader)
			if (!(tape instanceof Tape)) {
				throw new TapeLoadException("Expected a Tape but loaded a ${tape.getClass().name}")
			}
			tape
		} catch (YAMLException e) {
			throw new TapeLoadException("Invalid tape", e)
		}
	}

	void writeTape(Tape tape, Writer writer) {
		if (log.isDebugEnabled()) {
			def sw = new StringWriter()
			yaml.dump(tape, sw)
			log.debug sw.toString()
		}
		yaml.dump(tape, writer)
	}

	Yaml getYaml() {
		def representer = new TapeRepresenter()
		representer.addClassTag(Tape, "!tape")
		def constructor = new Constructor()
		constructor.addTypeDescription(new TypeDescription(Tape, "!tape"))
		new Yaml(constructor, representer, dumperOptions)
	}

}

