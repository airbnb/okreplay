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

package betamax

import betamax.server.HttpProxyServer
import betamax.storage.json.JsonTapeLoader
import groovy.util.logging.Log4j
import org.junit.rules.MethodRule
import betamax.storage.*
import org.junit.runners.model.*
import java.text.Normalizer

/**
 * This is the main interface to the Betamax proxy. It allows control of Betamax configuration and inserting and
 * ejecting `Tape` instances. The class can also be used as a _JUnit @Rule_ allowing tests annotated with `@Betamax` to
 * run with the Betamax HTTP proxy in the background.
 */
@Singleton
@Log4j
class Recorder implements MethodRule {

	/**
	 * The port the Betamax proxy will listen on.
	 */
	int proxyPort = 5555

	/**
	 * The base directory where tape files are stored.
	 */
	File tapeRoot = new File("src/test/resources/betamax/tapes")

	/**
	 * The strategy for reading and writing tape files.
	 */
	TapeLoader loader = new JsonTapeLoader()

	private Tape tape
	private HttpProxyServer proxy = new HttpProxyServer()

	/**
	 * Inserts a tape either creating a new one or loading an existing file from `tapeRoot`.
	 * @param name the name of the _tape_.
	 * @return the tape either loaded from file or newly created.
	 */
	Tape insertTape(String name) {
		def file = getTapeFile(name)
		if (file.isFile()) {
			file.withReader { reader ->
				tape = loader.readTape(reader)
			}
		} else {
			tape = new Tape(name: name)
		}
		tape
	}

	/**
	 * Gets the current active _tape_.
	 * @return the active _tape_.
	 */
	Tape getTape() {
		tape
	}

	/**
	 * 'Ejects' the current _tape_, writing its content to file. If the proxy is active after calling this method it
	 * will no longer record or play back any HTTP traffic until another tape is inserted.
	 * @return the ejected _tape_.
	 */
	Tape ejectTape() {
		if (tape) {
			def file = getTapeFile(tape.name)
			file.parentFile.mkdirs()
			log.debug "writing tape $tape to file $file.name"
			file.withWriter { writer ->
				loader.writeTape(tape, writer)
			}
		}
		def tapeToReturn = tape
		tape = null
		tapeToReturn
	}

	/**
	 * Runs the supplied closure after starting the Betamax proxy and inserting a _tape_. After the closure completes
	 * the _tape_ is ejected and the proxy stopped.
	 * @param name the name of the _tape_.
	 * @param closure the closure to execute.
	 * @return the _tape_ used when executing the closure.
	 */
	Tape withTape(String name, Closure closure) {
		try {
			proxy.start(this)
			insertTape(name)
			closure()
		} finally {
			proxy.stop()
			ejectTape()
		}
	}

	Statement apply(Statement statement, FrameworkMethod method, Object target) {
		def annotation = method.getAnnotation(Betamax)
		if (annotation) {
			log.debug "found @Betamax annotation on '$method.name'"
			new Statement() {
				void evaluate() {
					withTape(annotation.tape()) {
						statement.evaluate()
					}
				}
			}
		} else {
			log.debug "no @Betamax annotation on '$method.name'"
			statement
		}
	}

	private File getTapeFile(String name) {
		def filename = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll(/\p{InCombiningDiacriticalMarks}+/, "").replaceAll(/[^\w\d]+/, "_")
		new File(tapeRoot, "${filename}.json")
	}
}
