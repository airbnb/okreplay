package betamax

import betamax.storage.json.JsonTapeLoader
import betamax.storage.*

@Singleton
class Recorder {

	int proxyPort = 5555
	File tapeRoot = new File("src/test/resources/betamax/tapes")
	TapeLoader loader = new JsonTapeLoader()

	private Tape tape

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
}
