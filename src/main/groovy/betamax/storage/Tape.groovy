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

package betamax.storage

import betamax.TapeMode
import static betamax.TapeMode.READ_WRITE
import betamax.encoding.*
import org.apache.http.*
import static org.apache.http.HttpHeaders.CONTENT_ENCODING
import org.apache.http.entity.*
import org.apache.http.message.*

/**
 * Represents a set of recorded HTTP interactions that can be played back or appended to.
 */
class Tape {

	/**
	 * @return The name of the tape.
	 */
	String name
	List<RecordedInteraction> interactions = []
	private TapeMode mode = READ_WRITE
	private int position = -1

	/**
	 * @param mode the new record mode of the tape.
	 */
	void setMode(TapeMode mode) {
		this.mode = mode
	}

	/**
	 * @return `true` if the tape is readable, `false` otherwise.
	 */
	boolean isReadable() {
		mode.readable
	}

	/**
	 * @return `true` if the tape is writable, `false` otherwise.
	 */
	boolean isWritable() {
		mode.writable
	}

	/**
	 * @return the number of recorded HTTP interactions currently stored on the tape.
	 */
	int size() {
		interactions.size()
	}

	/**
	 * Attempts to find a recorded interaction on the tape that matches the supplied request's method and URI. If the
	 * method succeeds then subsequent calls to `play` will play back the response that was found.
	 * @param request the HTTP request to match.
	 * @return `true` if a matching recorded interaction was found, `false` otherwise.
	 */
	boolean seek(HttpRequest request) {
		position = interactions.findIndexOf {
			it.request.uri == request.requestLine.uri && it.request.method == request.requestLine.method
		}
		position >= 0
	}

	/**
	 * Resets the tape so that no recorded interaction is ready to play. Subsequent calls to `play` will throw
	 * `IllegalStateException` until a successful call to `seek` is made.
	 */
	void reset() {
		position = -1
	}

	/**
	 * Plays back a previously recorded interaction to the supplied response. Status, headers and entities are copied
	 * from the recorded interaction to `response`.
	 * @param response the HTTP response to populate.
	 * @throws IllegalStateException if no recorded interaction has been found by a previous call to `seek`.
	 */
	void play(HttpResponse response) {
		if (!mode.readable) {
			throw new IllegalStateException("the tape is not readable")
		} else if (position < 0) {
			throw new IllegalStateException("the tape is not ready to play")
		} else {
			def interaction = interactions[position]
			response.statusLine = new BasicStatusLine(parseProtocol(interaction.response.protocol), interaction.response.status, null)
			response.headers = interaction.response.headers.collect {
				new BasicHeader(it.key, it.value)
			}
			if (interaction.response.body instanceof byte[]) {
				response.entity = new ByteArrayEntity(interaction.response.body)
			} else if (interaction.response.headers[CONTENT_ENCODING] == "gzip") {
				response.entity = new ByteArrayEntity(new GzipEncoder().encode(interaction.response.body))
			} else if (interaction.response.headers[CONTENT_ENCODING] == "deflate") {
				response.entity = new ByteArrayEntity(new DeflateEncoder().encode(interaction.response.body))
			} else {
				response.entity = new StringEntity(interaction.response.body)
			}
			true
		}
	}

	/**
	 * Records a new interaction to the tape. If the tape is currently positioned to read a recorded interaction due to
	 * a previous successful `seek` call then this method will overwrite the existing recorded interaction. Otherwise
	 * the newly recorded interaction is appended to the tape.
	 * @param request the request to record.
	 * @param response the response to record.
	 * @throws UnsupportedOperationException if this `Tape` implementation is not writable.
	 */
	void record(HttpRequest request, HttpResponse response) {
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

	private static RecordedRequest recordRequest(HttpEntityEnclosingRequest request) {
		def clone = new RecordedRequest()
		clone.protocol = request.requestLine.protocolVersion.toString()
		clone.method = request.requestLine.method
		clone.uri = request.requestLine.uri
		clone.headers = request.allHeaders.collectEntries { [it.name, it.value] }
		clone.body = request.entity.content.text // TODO: handle encoded request bodies
		clone
	}

	private static RecordedRequest recordRequest(HttpRequest request) {
		def clone = new RecordedRequest()
		clone.protocol = request.requestLine.protocolVersion.toString()
		clone.method = request.requestLine.method
		clone.uri = request.requestLine.uri
		clone.headers = request.allHeaders.collectEntries { [it.name, it.value] }
		clone.body = null
		clone
	}

	private static RecordedResponse recordResponse(HttpResponse response) {
		def clone = new RecordedResponse()
		clone.protocol = response.statusLine.protocolVersion.toString()
		clone.status = response.statusLine.statusCode
		clone.headers = response.allHeaders.collectEntries { [it.name, it.value] }
		clone.body = recordEntity(response.entity, response.getFirstHeader(CONTENT_ENCODING)?.value)
		clone
	}

	private static recordEntity(HttpEntity entity, String contentEncoding) {
		if (!entity) {
			null
		} else if (entity instanceof StringEntity) {
			entity.content.text
		} else if (contentEncoding == "gzip") {
			new GzipEncoder().decode(entity.content)
		} else if (contentEncoding == "deflate") {
			new DeflateEncoder().decode(entity.content)
		} else {
			def bytes = new ByteArrayOutputStream()
			entity.writeTo(bytes)
			bytes.toByteArray()
		}
	}

	private ProtocolVersion parseProtocol(String protocolString) {
		def matcher = protocolString =~ /^(\w+)\/(\d+)\.(\d+)$/
		new ProtocolVersion(matcher[0][1], matcher[0][2].toInteger(), matcher[0][3].toInteger())
	}

}

class RecordedInteraction {
	Date recorded
	RecordedRequest request
	RecordedResponse response
}

class RecordedRequest {
	String protocol
	String method
	String uri
	Map<String, String> headers
	String body
}

class RecordedResponse {
	String protocol
	int status
	Map<String, String> headers
	def body
}
