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

import groovy.util.logging.Log4j
import java.text.SimpleDateFormat
import org.apache.http.entity.StringEntity
import betamax.storage.*
import groovy.json.*
import org.apache.http.*
import org.apache.http.message.*

@Log4j
class JsonTapeLoader extends AbstractTapeLoader {

	static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss Z"

	String getFileExtension() {
		"json"
	}

	Tape readTape(Reader reader) {
		try {
			def json = new JsonSlurper().parse(reader)
			require json, "tape"

			def tape = new Tape()
			require json.tape, "name", "interactions"
			tape.name = json.tape.name
			json.tape.interactions.each {
				tape.interactions << loadInteraction(it)
			}
			tape
		} catch (java.text.ParseException e) {
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

	private HttpInteraction loadInteraction(Map json) {
		require json, "request", "response", "recorded"
		def request = loadRequest(json.request)
		def response = loadResponse(json.response)
		def recorded = new SimpleDateFormat(TIMESTAMP_FORMAT).parse(json.recorded)
		new HttpInteraction(request: request, response: response, recorded: recorded)
	}

	private HttpRequest loadRequest(Map json) {
		require json, "protocol", "method", "uri"
		def requestProtocol = parseProtocol(json.protocol)
		new BasicHttpRequest(json.method, json.uri, requestProtocol)
	}

	private HttpResponse loadResponse(Map json) {
		require json, "protocol", "status"
		def responseProtocol = parseProtocol(json.protocol)
		def response = new BasicHttpResponse(responseProtocol, json.status, null)
		response.entity = new StringEntity(json.body)
		json.headers.each { header ->
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
