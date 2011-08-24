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

import groovy.util.logging.Log4j
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.yaml.snakeyaml.DumperOptions.FlowStyle
import betamax.encoding.*
import betamax.storage.*
import org.apache.http.*
import static org.apache.http.HttpHeaders.CONTENT_ENCODING
import org.apache.http.entity.*
import org.apache.http.message.*
import org.yaml.snakeyaml.*

@Log4j
class YamlTapeLoader extends AbstractTapeLoader {

	/**
	 * Options controlling the style of the YAML written out.
	 */
	DumperOptions dumperOptions = new DumperOptions(defaultFlowStyle: FlowStyle.BLOCK)

	String getFileExtension() {
		"yaml"
	}

	Tape readTape(Reader reader) {
		try {
			def yaml = new Yaml()
			toTape(yaml.load(reader))
		} catch (GroovyCastException e) {
			throw new TapeLoadException("Invalid tape", e)
		}
	}

	void writeTape(Tape tape, Writer writer) {
		def map = [tape: [name: tape.name, interactions: data(tape.interactions)]]
		def yaml = new Yaml(dumperOptions)
		yaml.dump(map, writer)
	}

	private Tape toTape(Map data) {
		require data, "tape"
		def tape = new Tape()
		require data.tape, "name", "interactions"
		tape.name = data.tape.name
		data.tape.interactions.each {
			tape.interactions << toInteraction(it)
		}
		tape
	}

	private HttpInteraction toInteraction(Map data) {
		require data, "request", "response", "recorded"
		def request = toRequest(data.request)
		def response = loadResponse(data.response)
		def recorded = data.recorded
		new HttpInteraction(request: request, response: response, recorded: recorded)
	}

	private HttpRequest toRequest(Map data) {
		require data, "protocol", "method", "uri"
		def requestProtocol = parseProtocol(data.protocol)
		def request = new BasicHttpRequest(data.method, data.uri, requestProtocol)
		data.headers.each { header ->
			request.addHeader(header.key, header.value)
		}
		request
	}

	private HttpResponse loadResponse(Map data) {
		require data, "protocol", "status"
		def responseProtocol = parseProtocol(data.protocol)
		def response = new BasicHttpResponse(responseProtocol, data.status, null)
		switch (data.body) {
			case String:
				if (data.headers[CONTENT_ENCODING] == "gzip") {
					response.entity = new ByteArrayEntity(new GzipEncoder().encode(data.body))
				} else if (data.headers[CONTENT_ENCODING] == "deflate") {
					response.entity = new ByteArrayEntity(new DeflateEncoder().encode(data.body))
				} else {
					response.entity = new StringEntity(data.body)
				}
				break
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

	private void require(Map map, String... keys) {
		for (key in keys) {
			if (!map.containsKey(key)) {
				throw new TapeLoadException("Missing element '$key'")
			}
		}
	}

}
