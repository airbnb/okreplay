package co.freeside.betamax.handler

import co.freeside.betamax.Recorder
import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response

import java.util.logging.Logger

import static java.net.HttpURLConnection.HTTP_FORBIDDEN
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
			throw new HandlerException(HTTP_FORBIDDEN, 'No tape')
		} else if (!tape.writable) {
			throw new HandlerException(HTTP_FORBIDDEN, 'Tape is read-only')
		}

		def response = chain(request)
		log.log INFO, "Recording to '$tape.name'"
		tape.record(request, response)

		response.addHeader('X-Betamax', 'REC')

		response
	}

}
