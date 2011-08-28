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

import org.apache.http.util.EntityUtils
import betamax.*
import static betamax.TapeMode.READ_WRITE
import betamax.encoding.*
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import org.apache.http.entity.*
import org.apache.http.message.*

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

	boolean seek(HttpRequest request) {
		position = interactions.findIndexOf {
			it.request.uri == request.requestLine.uri && it.request.method == request.requestLine.method
		}
		position >= 0
	}

	void reset() {
		position = -1
	}

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
			} else {
				def mimeType = indentifyMimeType(response.getFirstHeader(CONTENT_TYPE))
				def charset = identifyCharset(response.getFirstHeader(CONTENT_TYPE))
				def encoding = response.getFirstHeader(CONTENT_ENCODING)?.value
				if (encoding == "gzip") {
					response.entity = new ByteArrayEntity(new GzipEncoder().encode(interaction.response.body, charset))
				} else if (encoding == "deflate") {
					response.entity = new ByteArrayEntity(new DeflateEncoder().encode(interaction.response.body, charset))
				} else {
					response.entity = new StringEntity(interaction.response.body, mimeType, charset)
				}
			}
			true
		}
	}

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
		clone.body = recordEntity(response.entity)
		clone
	}

	private static recordEntity(HttpEntity entity) {
		if (!entity) {
			return null
		}

		def encoding = entity.contentEncoding?.value
		def charset = EntityUtils.getContentCharSet(entity)
		if (entity instanceof StringEntity) {
			EntityUtils.toString(entity, charset)
		} else if (encoding == "gzip") {
			new GzipEncoder().decode(entity.content, charset)
		} else if (encoding == "deflate") {
			new DeflateEncoder().decode(entity.content, charset)
		} else if (charset) {
			EntityUtils.toString(entity, charset)
		} else {
			EntityUtils.toByteArray(entity)
		}
	}

	private String indentifyMimeType(Header contentType) {
		contentType?.elements[0]?.name
	}

	private String identifyCharset(Header contentType) {
		contentType?.elements[0]?.getParameterByName("charset")?.value
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
