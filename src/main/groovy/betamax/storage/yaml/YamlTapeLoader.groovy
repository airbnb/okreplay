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
import org.yaml.snakeyaml.representer.Representer
import org.yaml.snakeyaml.introspector.Property

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
			def yaml = new Yaml(new GroovyRepresenter())
			toTape(yaml.load(reader))
		} catch (GroovyCastException e) {
			throw new TapeLoadException("Invalid tape", e)
		}
	}

	void writeTape(Tape tape, Writer writer) {
		def yaml = new Yaml(new GroovyRepresenter(), dumperOptions)
		yaml.dump(tape, writer)
	}

	private List<Map> data(Collection<TapeInteraction> interactions) {
		interactions.collect {
			[recorded: it.recorded, request: data(it.request), response: data(it.response)]
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

	private TapeInteraction toInteraction(Map data) {
		require data, "request", "response", "recorded"
		def request = toRequest(data.request)
		def response = loadResponse(data.response)
		def recorded = data.recorded
		new TapeInteraction(request: request, response: response, recorded: recorded)
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

class GroovyRepresenter extends Representer {
	@Override
	protected Set<Property> getProperties(Class<? extends Object> type) {
		def set = super.getProperties(type)
		set.removeAll {
			it.name == "metaClass"
		}
		set
	}

}
