package betamax.storage

interface TapeLoader {

	Tape readTape(Reader reader)

	void writeTape(Tape tape, Writer writer)

}