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

import betamax.encoding.*
import org.apache.http.*
import static org.apache.http.HttpHeaders.CONTENT_ENCODING
import org.apache.http.entity.*
import org.apache.http.message.*

class Tape {

	String name
	List<TapeInteraction> interactions = []
	private int position = -1

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
		if (position >= 0) {
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
		} else {
			throw new IllegalStateException("Tape is not ready to play")
		}
	}

	void record(HttpRequest request, HttpResponse response) {
		interactions << new TapeInteraction(request: recordRequest(request), response: recordResponse(response), recorded: new Date())
	}

	@Override
	String toString() {
		"Tape[$name]"
	}

	private static TapeRequest recordRequest(HttpEntityEnclosingRequest request) {
		def clone = new TapeRequest()
		clone.protocol = request.requestLine.protocolVersion.toString()
		clone.method = request.requestLine.method
		clone.uri = request.requestLine.uri
		clone.headers = request.allHeaders.collectEntries { [it.name, it.value] }
		clone.body = request.entity.content.text // TODO: handle encoded request bodies
		clone
	}

	private static TapeRequest recordRequest(HttpRequest request) {
		def clone = new TapeRequest()
		clone.protocol = request.requestLine.protocolVersion.toString()
		clone.method = request.requestLine.method
		clone.uri = request.requestLine.uri
		clone.headers = request.allHeaders.collectEntries { [it.name, it.value] }
		clone.body = null
		clone
	}

	private static TapeResponse recordResponse(HttpResponse response) {
		def clone = new TapeResponse()
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

	// TODO: duplicated in betamax.storage.yaml.YamlTapeLoader
	private ProtocolVersion parseProtocol(String protocolString) {
		def matcher = protocolString =~ /^(\w+)\/(\d+)\.(\d+)$/
		new ProtocolVersion(matcher[0][1], matcher[0][2].toInteger(), matcher[0][3].toInteger())
	}
}

class TapeInteraction {

	Date recorded
	TapeRequest request
	TapeResponse response

}

class TapeRequest {
	String protocol
	String method
	String uri
	Map<String, String> headers
	String body
}

class TapeResponse {
	String protocol
	int status
	Map<String, String> headers
	def body
}
