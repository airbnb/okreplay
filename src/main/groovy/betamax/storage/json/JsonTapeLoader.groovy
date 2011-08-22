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

package betamax.storage.json

import java.text.*
import org.apache.http.entity.StringEntity
import betamax.storage.*
import groovy.json.*
import static org.apache.commons.codec.binary.Base64.encodeBase64String
import org.apache.http.*
import org.apache.http.message.*
import groovy.util.logging.Log4j

@Log4j
class JsonTapeLoader implements TapeLoader {

	static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss Z"

	Tape readTape(Reader reader) {
		try {
			def json = new JsonSlurper().parse(reader)

			def tape = new Tape()
			tape.name = json.tape.name
			json.tape.interactions.each {
				def requestProtocol = parseProtocol(it.request.protocol)
				def request = new BasicHttpRequest(it.request.method, it.request.uri, requestProtocol)
				def responseProtocol = parseProtocol(it.response.protocol)
				def response = new BasicHttpResponse(responseProtocol, it.response.status, null)
				response.entity = new StringEntity(it.response.body)
				it.response.headers.each { header ->
					response.addHeader(header.key, header.value)
				}
				def recorded = new SimpleDateFormat(TIMESTAMP_FORMAT).parse(it.recorded)

				def interaction = new HttpInteraction(request: request, response: response, recorded: recorded)
				tape.interactions << interaction
			}

			tape
		} catch (ParseException e) {
			throw new TapeLoadException("Invalid tape", e)
		} catch (JsonException e) {
			throw new TapeLoadException("Invalid tape", e)
		}
	}

	void writeTape(Tape tape, Writer writer) {
		def json = new JsonBuilder()
		json.tape {
			name tape.name
			interactions data(tape.interactions)
		}
		writer << json.toPrettyString()
	}

	private List<Map> data(Collection<HttpInteraction> interactions) {
		interactions.collect {
			[recorded: new SimpleDateFormat(TIMESTAMP_FORMAT).format(it.recorded), request: data(it.request), response: data(it.response)]
		}
	}

	private Map data(HttpRequest request) {
		def map = [
				protocol: request.requestLine.protocolVersion,
				method: request.requestLine.method,
				uri: request.requestLine.uri
		]
		if (request instanceof HttpEntityEnclosingRequest) {
			map.body = request.entity.content.text
		}
		map
	}

	private Map data(HttpResponse response) {
		def map = [
				protocol: response.statusLine.protocolVersion,
				status: response.statusLine.statusCode
		]
		if (response.entity) {
			log.debug "got entity with content type '${response.entity.contentType?.value}'"
			map.body = response.entity.content.text
		}
		map
	}

	private ProtocolVersion parseProtocol(String protocolString) {
		def matcher = protocolString =~ /^(\w+)\/(\d+)\.(\d+)$/
		new ProtocolVersion(matcher[0][1], matcher[0][2].toInteger(), matcher[0][3].toInteger())
	}
}
