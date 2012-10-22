package co.freeside.betamax.handler

import java.util.logging.Logger
import co.freeside.betamax.Recorder
import co.freeside.betamax.handler.*
import co.freeside.betamax.message.*
import static co.freeside.betamax.proxy.jetty.BetamaxProxy.X_BETAMAX
import static java.util.logging.Level.INFO

class TapeWriter extends ChainedHttpHandler {

	private final Recorder recorder

	private static final Logger log = Logger.getLogger(TapeWriter.name)

	TapeWriter(Recorder recorder) {
		this.recorder = recorder
	}

	Response handle(Request request) {
		def tape = recorder.tape
		if (!tape) {
			throw new NoTapeException()
		} else if (!tape.writable) {
			throw new NonWritableTapeException()
		}

		def response = chain(request)
		log.log INFO, "Recording to '$tape.name'"
		tape.record(request, response)

		response.addHeader(X_BETAMAX, 'REC')

		response
	}

}
