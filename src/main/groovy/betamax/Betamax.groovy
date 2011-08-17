package betamax

import betamax.storage.Tape

@Singleton
class Betamax {

	int proxyPort = 5555
    File tapeRoot = new File("src/test/resources/betamax/tapes")

	private Tape tape

	void insertTape(String name) {
		tape = new Tape(name: name)
	}

	Tape getTape() {
		tape
	}

}
