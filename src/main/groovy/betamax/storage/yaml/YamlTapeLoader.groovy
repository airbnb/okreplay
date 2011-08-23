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

package betamax.storage.yaml

import betamax.storage.TapeLoader
import betamax.storage.Tape
import org.yaml.snakeyaml.Yaml
import betamax.storage.HttpInteraction
import java.text.SimpleDateFormat
import org.apache.http.HttpRequest
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpResponse
import groovy.util.logging.Log4j
import betamax.storage.TapeLoadException
import org.apache.http.message.BasicHttpRequest
import org.apache.http.message.BasicHttpResponse
import org.apache.http.entity.StringEntity
import org.apache.http.ProtocolVersion
import org.apache.http.entity.ByteArrayEntity

@Log4j
class YamlTapeLoader implements TapeLoader {

	static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss Z"

	String getFileExtension() {
		"yaml"
	}

	Tape readTape(Reader reader) {
		try {
			def yaml = new Yaml()
			toTape(yaml.load(reader))
		} catch (java.text.ParseException e) {
			throw new TapeLoadException("Invalid tape", e)
		}
	}

	void writeTape(Tape tape, Writer writer) {
		def map = [tape: [name: tape.name, interactions: data(tape.interactions)]]
		def yaml = new Yaml()
		yaml.dump(map, writer)
	}

	private List<Map> data(Collection<HttpInteraction> interactions) {
		interactions.collect {
			[recorded: new SimpleDateFormat(TIMESTAMP_FORMAT).format(it.recorded), request: data(it.request), response: data(it.response)]
		}
	}

	private Map data(HttpRequest request) {
		def map = [
				protocol: request.requestLine.protocolVersion.toString(),
				method: request.requestLine.method,
				uri: request.requestLine.uri,
				headers: request.allHeaders.collectEntries { [it.name, it.value] }
		]
		if (request instanceof HttpEntityEnclosingRequest) {
			map.body = request.entity.content.text
		}
		map
	}

	private Map data(HttpResponse response) {
		def map = [
				protocol: response.statusLine.protocolVersion.toString(),
				status: response.statusLine.statusCode,
				headers: response.allHeaders.collectEntries { [it.name, it.value] }
		]
		if (response.entity) {
			map.body = response.entity.content.text
		}
		map
	}

	private Tape toTape(data) {
		require data, "tape"
		def tape = new Tape()
		require data.tape, "name", "interactions"
		tape.name = data.tape.name
		data.tape.interactions.each {
			tape.interactions << toInteraction(it)
		}
		tape
	}

	private HttpInteraction toInteraction(data) {
		require data, "request", "response", "recorded"
		def request = toRequest(data.request)
		def response = loadResponse(data.response)
		def recorded = new SimpleDateFormat(TIMESTAMP_FORMAT).parse(data.recorded)
		new HttpInteraction(request: request, response: response, recorded: recorded)
	}

	private HttpRequest toRequest(data) {
		require data, "protocol", "method", "uri"
		def requestProtocol = parseProtocol(data.protocol)
		new BasicHttpRequest(data.method, data.uri, requestProtocol)
	}

	private HttpResponse loadResponse(data) {
		require data, "protocol", "status"
		def responseProtocol = parseProtocol(data.protocol)
		def response = new BasicHttpResponse(responseProtocol, data.status, null)
		switch (data.body) {
			case String:
				response.entity = new StringEntity(data.body); break
			case byte[]:
				response.entity = new ByteArrayEntity(data.body); break
			default:
				throw new TapeLoadException("Unhandled body type ${data.body.getClass().name}")
		}
		data.headers.each { header ->
			response.addHeader(header.key, header.value)
		}
		response
	}

	private ProtocolVersion parseProtocol(String protocolString) {
		def matcher = protocolString =~ /^(\w+)\/(\d+)\.(\d+)$/
		new ProtocolVersion(matcher[0][1], matcher[0][2].toInteger(), matcher[0][3].toInteger())
	}

	private void require(Map map, String... keys) {
		for (key in keys) {
			if (!map.containsKey(key)) {
				throw new TapeLoadException("Missing element '$key'")
			}
		}
	}

}
