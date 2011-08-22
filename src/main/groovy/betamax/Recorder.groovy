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

@Singleton
@Log4j
class Recorder implements MethodRule {

	int proxyPort = 5555
	File tapeRoot = new File("src/test/resources/betamax/tapes")
	TapeLoader loader = new JsonTapeLoader()

	private Tape tape
	private HttpProxyServer proxy = new HttpProxyServer()

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

	Tape getTape() {
		tape
	}

	Tape ejectTape() {
		if (tape) {
			def file = getTapeFile(tape.name)
			file.parentFile.mkdirs()
			file.withWriter { writer ->
				loader.writeTape(tape, writer)
			}
		}
		def tapeToReturn = tape
		tape = null
		tapeToReturn
	}

	private File getTapeFile(String name) {
		new File(tapeRoot, "${name}.json")
	}

	Tape withTape(String name, Closure closure) {
		insertTape(name)
		closure.call(tape)
		ejectTape()
	}

	Statement apply(Statement statement, FrameworkMethod method, Object target) {
		def annotation = method.getAnnotation(Betamax)
		if (annotation) {
			log.debug "found Betamax annotation on $method.name"
			new Statement() {
				void evaluate() {
					withProxy {
						try {
							insertTape(annotation.tape())
							statement.evaluate()
						} finally {
							ejectTape()
						}
					}
				}
			}
		} else {
			log.debug "no Betamax annotation on $method.name"
			statement
		}
	}

	private def withProxy(Closure closure) {
		try {
			proxy.start(this)
			closure()
		} finally {
			proxy.stop()
		}
	}
}
