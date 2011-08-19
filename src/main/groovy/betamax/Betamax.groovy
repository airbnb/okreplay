package betamax

import betamax.storage.Tape

@Singleton
class Betamax {

	int proxyPort = 5555
	File tapeRoot = new File("src/test/resources/betamax/tapes")

	private Tape tape

	void insertTape(String name) {
		def file = getTapeFile(name)
		if (file.isFile()) {
			file.withReader { reader ->
				tape = new Tape(reader)
			}
		} else {
			tape = new Tape(name)
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
				writer << tape
			}
		}
	}

	private File getTapeFile(String name) {
		new File(tapeRoot, "${name}.json")
	}
}
