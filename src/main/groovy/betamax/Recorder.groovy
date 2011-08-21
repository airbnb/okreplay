package betamax

import betamax.storage.json.JsonTapeLoader
import betamax.storage.*

@Singleton
class Recorder {

	int proxyPort = 5555
	File tapeRoot = new File("src/test/resources/betamax/tapes")
	TapeLoader loader = new JsonTapeLoader()

	private Tape tape

	void insertTape(String name) {
		def file = getTapeFile(name)
		if (file.isFile()) {
			file.withReader { reader ->
				tape = loader.readTape(reader)
			}
		} else {
			tape = new Tape(name: name)
		}
	}

	Tape getTape() {
		tape
	}

	void ejectTape() {
		if (tape) {
			def file = getTapeFile(tape.name)
			file.parentFile.mkdirs()
			file.withWriter { writer ->
				loader.writeTape(tape, writer)
			}
		}
	}

	private File getTapeFile(String name) {
		new File(tapeRoot, "${name}.json")
	}
}
