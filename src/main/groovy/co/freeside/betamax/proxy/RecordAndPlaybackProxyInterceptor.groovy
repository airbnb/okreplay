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

package co.freeside.betamax.proxy

import co.freeside.betamax.Recorder

import java.util.logging.Logger

import static java.net.HttpURLConnection.HTTP_FORBIDDEN

class RecordAndPlaybackProxyInterceptor implements VetoingProxyInterceptor {

	/**
	 * Header placed in the response to indicate whether the response was recorded or played back.
	 */
	public static final String X_BETAMAX = "X-Betamax"

	private final Recorder recorder
	private final Logger log = Logger.getLogger(RecordAndPlaybackProxyInterceptor.name)

	RecordAndPlaybackProxyInterceptor(Recorder recorder) {
		this.recorder = recorder
	}

	boolean interceptRequest(Request request, Response response) {
		def tape = recorder.tape
		if (!tape) {
			log.severe "no tape inserted..."
			response.setError(HTTP_FORBIDDEN, "No tape")
			true
		} else if (tape.seek(request) && tape.isReadable()) {
			log.info "playing back from tape '$tape.name'..."
			response.addHeader(X_BETAMAX, "PLAY")
			tape.play(request, response)
			true
		} else if (!tape.isWritable()) {
			response.setError(HTTP_FORBIDDEN, "Tape is read-only")
			true
		} else {
			response.addHeader(X_BETAMAX, "REC")
			false
		}
	}

	void interceptResponse(Request request, Response response) {
		def tape = recorder.tape
		log.info "recording response with status $response.status to tape '$tape.name'..."
		tape.record(request, response)
		log.info "recording complete..."
	}

}
