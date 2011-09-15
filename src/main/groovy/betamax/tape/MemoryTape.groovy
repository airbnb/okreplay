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

package betamax.tape

import betamax.*
import static betamax.TapeMode.READ_WRITE
import betamax.proxy.*
import static betamax.proxy.RecordAndPlaybackProxyInterceptor.X_BETAMAX
import static org.apache.http.HttpHeaders.VIA

/**
 * Represents a set of recorded HTTP interactions that can be played back or appended to.
 */
class MemoryTape implements Tape {

	String name
	List<RecordedInteraction> interactions = []
	private TapeMode mode = READ_WRITE
	private int position = -1

	void setMode(TapeMode mode) {
		this.mode = mode
	}

	boolean isReadable() {
		mode.readable
	}

	boolean isWritable() {
		mode.writable
	}

	int size() {
		interactions.size()
	}

	boolean seek(Request request) {
		position = interactions.findIndexOf {
			it.request.uri == request.target.toString() && it.request.method == request.method
		}
		position >= 0
	}

	void reset() {
		position = -1
	}

	void play(Response response) {
		if (!mode.readable) {
			throw new IllegalStateException("the tape is not readable")
		} else if (position < 0) {
			throw new IllegalStateException("the tape is not ready to play")
		} else {
			def interaction = interactions[position]
			response.status = interaction.response.status
			interaction.response.headers.each {
				response.addHeader(it.key, it.value)
			}
			if (interaction.response.body instanceof String) {
				response.writer.withWriter {
					it << interaction.response.body
				}
			} else {
				response.outputStream.withStream {
					it << interaction.response.body
				}
			}
			true
		}
	}

	void record(Request request, Response response) {
		if (mode.writable) {
			def interaction = new RecordedInteraction(request: recordRequest(request), response: recordResponse(response), recorded: new Date())
			if (position >= 0) {
				interactions[position] = interaction
			} else {
				interactions << interaction
			}
		} else {
			throw new IllegalStateException("the tape is not writable")
		}
	}

	@Override
	String toString() {
		"Tape[$name]"
	}

	private static RecordedRequest recordRequest(Request request) {
		def clone = new RecordedRequest()
		clone.method = request.method
		clone.uri = request.target
		request.headers.each {
			if (it.key != VIA) {
				clone.headers[it.key] = it.value.join(", ")
			}
		}
		clone.body = request.hasBody() ? request.bodyAsText.text : null // TODO: handle encoded request bodies
		clone
	}

	private static RecordedResponse recordResponse(Response response) {
		def clone = new RecordedResponse()
		clone.status = response.status
		response.headers.each {
			if (!(it.key in [VIA, X_BETAMAX])) {
				clone.headers[it.key] = it.value.join(", ")
			}
		}
		if (response.hasBody()) {
			clone.body = isTextContentType(response.contentType) ? response.bodyAsText.text : response.bodyAsBinary.bytes
		}
		clone
	}

	static boolean isTextContentType(String contentType) {
		if (contentType) {
			contentType.startsWith("text/") || contentType in ["application/json", "application/javascript"]
		} else {
			false
		}
	}

}

class RecordedInteraction {
	Date recorded
	RecordedRequest request
	RecordedResponse response
}

class RecordedRequest {
	String method
	String uri
	Map<String, String> headers = [:]
	String body
}

class RecordedResponse {
	int status
	Map<String, String> headers = [:]
	def body
}
