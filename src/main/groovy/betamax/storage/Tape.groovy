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

import org.apache.http.entity.ByteArrayEntity
import org.apache.http.*
import org.apache.http.message.*

class Tape {

	String name
	String description
	Collection<HttpInteraction> interactions = []

	boolean play(HttpRequest request, HttpResponse response) {
		def interaction = interactions.find {
			it.request.requestLine.uri == request.requestLine.uri && it.request.requestLine.method == request.requestLine.method
		}
		if (interaction) {
			response.statusLine = interaction.response.statusLine
			response.headers = interaction.response.allHeaders
			response.entity = interaction.response.entity
			true
		} else {
			false
		}
	}

	void record(HttpRequest request, HttpResponse response) {
		interactions << new HttpInteraction(request: cloneRequest(request), response: cloneResponse(response), recorded: new Date())
	}

	@Override
	String toString() {
		"Tape[$name]"
	}

	private static HttpEntityEnclosingRequest cloneRequest(HttpEntityEnclosingRequest request) {
		def clone = new BasicHttpEntityEnclosingRequest(request.requestLine)
		clone.entity = cloneEntity(request.entity)
		clone
	}

	private static HttpRequest cloneRequest(HttpRequest request) {
		def clone = new BasicHttpRequest(request.requestLine)
		clone.headers = request.allHeaders
		clone
	}

	private static HttpResponse cloneResponse(HttpResponse response) {
		def clone = new BasicHttpResponse(response.statusLine)
		clone.headers = response.allHeaders
		clone.entity = cloneEntity(response.entity)
		clone
	}

	private static HttpEntity cloneEntity(HttpEntity entity) {
		if (entity) {
			def bytes = new ByteArrayOutputStream()
			entity.writeTo(bytes)
			new ByteArrayEntity(bytes.toByteArray())
		} else {
			null
		}
	}

}

class HttpInteraction {

	HttpRequest request
	HttpResponse response
	String description
	Date recorded

}
