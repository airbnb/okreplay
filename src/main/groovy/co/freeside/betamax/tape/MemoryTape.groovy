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

package co.freeside.betamax.tape

import co.freeside.betamax.*
import co.freeside.betamax.proxy.*
import org.yaml.snakeyaml.reader.StreamReader

import static TapeMode.READ_WRITE
import static co.freeside.betamax.MatchRule.*
import static co.freeside.betamax.proxy.RecordAndPlaybackProxyInterceptor.X_BETAMAX
import static org.apache.http.HttpHeaders.*

/**
 * Represents a set of recorded HTTP interactions that can be played back or appended to.
 */
class MemoryTape implements Tape {

	String name
	List<RecordedInteraction> interactions = []
	private TapeMode mode = READ_WRITE
	private Comparator<Request>[] matchRules = [method, uri]

	void setMode(TapeMode mode) {
		this.mode = mode
	}

	void setMatchRules(Comparator<Request>[] matchRules) {
		this.matchRules = matchRules
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
		def requestMatcher = new RequestMatcher(request, matchRules)
		interactions.any {
			requestMatcher.matches(it.request)
		}
	}

	void play(Request request, Response response) {
		if (!mode.readable) throw new IllegalStateException("the tape is not readable")

		int position = findMatch(request)
		if (position < 0) {
			throw new IllegalStateException("no matching recording found")
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
		if (!mode.writable) throw new IllegalStateException("the tape is not writable")

		def interaction = new RecordedInteraction(request: recordRequest(request), response: recordResponse(response), recorded: new Date())
		int position = findMatch(request)
		if (position >= 0) {
			interactions[position] = interaction
		} else {
			interactions << interaction
		}
	}

	@Override
	String toString() {
		"Tape[$name]"
	}

	private int findMatch(Request request) {
		def requestMatcher = new RequestMatcher(request, matchRules)
		interactions.findIndexOf {
			requestMatcher.matches(it.request)
		}
	}

	private static RecordedRequest recordRequest(Request request) {
		def clone = new RecordedRequest()
		clone.method = request.method
		clone.uri = request.uri
		request.headers.each {
			if (it.key != VIA) {
				clone.headers[it.key] = it.value
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
				clone.headers[it.key] = it.value
			}
		}
		if (response.hasBody()) {
			clone.body = isTextContentType(response.contentType) && isPrintable(response.bodyAsText.text) ? response.bodyAsText.text : response.bodyAsBinary.bytes
		}
		clone
	}

	static boolean isTextContentType(String contentType) {
		if (contentType) {
			contentType =~ /^text\/|application\/(json|javascript|(\w+\+)?xml)/
		} else {
			false
		}
	}

	static boolean isPrintable(String s) {
		// this check is performed by SnakeYaml but we need to do so *before* unzipping the byte stream otherwise we
		// won't be able to read it back again.
		!(s =~ StreamReader.NON_PRINTABLE)
	}

}

class RecordedInteraction {
	Date recorded
	RecordedRequest request
	RecordedResponse response
}

class RecordedRequest implements Request {
	String method
	URI uri
	Map<String, String> headers = [:]
	String body

	String getHeader(String name) {
		headers[name]
	}

	boolean hasBody() {
		body
	}

	Reader getBodyAsText() {
		new InputStreamReader(bodyAsBinary) // TODO: charset
	}

	InputStream getBodyAsBinary() {
		new ByteArrayInputStream(body.bytes)
	}
}

class RecordedResponse implements Response {
	int status
	Map<String, String> headers = [:]
	def body

	String getHeader(String name) {
		headers[name]
	}

	boolean hasBody() {
		body
	}

	Reader getBodyAsText() {
		body instanceof String ? new StringReader(body) : new InputStreamReader(bodyAsBinary) // TODO: charset
	}

	InputStream getBodyAsBinary() {
		body instanceof String ? new ByteArrayInputStream(body.bytes) : new ByteArrayInputStream(body)
	}

	String getContentType() {
		headers[CONTENT_TYPE]
	}
}
