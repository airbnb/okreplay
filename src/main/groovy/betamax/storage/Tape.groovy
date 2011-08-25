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

import org.apache.http.entity.StringEntity
import org.apache.http.*
import org.apache.http.message.*

class Tape {

	String name
	String description
	Collection<TapeInteraction> interactions = []

	boolean play(HttpRequest request, HttpResponse response) {
		def interaction = interactions.find {
			it.request.uri == request.requestLine.uri && it.request.method == request.requestLine.method
		}
		if (interaction) {
			response.statusLine = new BasicStatusLine(parseProtocol(interaction.response.protocol), interaction.response.status, null)
			response.headers = interaction.response.headers.collect {
				new BasicHeader(it.key, it.value)
			}
			response.entity = new StringEntity(interaction.response.body)
			true
		} else {
			false
		}
	}

	void record(HttpRequest request, HttpResponse response) {
		interactions << new TapeInteraction(request: cloneRequest(request), response: cloneResponse(response), recorded: new Date())
	}

	@Override
	String toString() {
		"Tape[$name]"
	}

	private static TapeRequest cloneRequest(HttpEntityEnclosingRequest request) {
		def clone = new TapeRequest()
		clone.protocol = request.requestLine.protocolVersion.toString()
		clone.method = request.requestLine.method
		clone.uri = request.requestLine.uri
		clone.headers = request.allHeaders.collectEntries { [it.name, it.value] }
		clone.body = cloneEntity(request.entity)
		clone
	}

	private static TapeRequest cloneRequest(HttpRequest request) {
		def clone = new TapeRequest()
		clone.protocol = request.requestLine.protocolVersion.toString()
		clone.method = request.requestLine.method
		clone.uri = request.requestLine.uri
		clone.headers = request.allHeaders.collectEntries { [it.name, it.value] }
		clone.body = null
		clone
	}

	private static TapeResponse cloneResponse(HttpResponse response) {
		def clone = new TapeResponse()
		clone.protocol = response.statusLine.protocolVersion.toString()
		clone.status = response.statusLine.statusCode
		clone.headers = response.allHeaders.collectEntries { [it.name, it.value] }
		clone.body = cloneEntity(response.entity)
		clone
	}

	private static String cloneEntity(HttpEntity entity) {
		if (entity) {
			def bytes = new ByteArrayOutputStream()
			entity.writeTo(bytes)
			bytes.toString("UTF-8")
		} else {
			null
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
	String description
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
	String body
}
