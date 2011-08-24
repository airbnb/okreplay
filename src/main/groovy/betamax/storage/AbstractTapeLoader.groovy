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

import org.apache.http.*
import static org.apache.http.HttpHeaders.CONTENT_ENCODING
import betamax.encoding.GzipEncoder

abstract class AbstractTapeLoader implements TapeLoader {

	protected List<Map> data(Collection<HttpInteraction> interactions) {
		interactions.collect {
			[recorded: it.recorded, request: data(it.request), response: data(it.response)]
		}
	}

	protected Map data(HttpRequest request) {
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

	protected Map data(HttpResponse response) {
		def map = [
				protocol: response.statusLine.protocolVersion.toString(),
				status: response.statusLine.statusCode,
				headers: response.allHeaders.collectEntries { [it.name, it.value] }
		]
		if (response.entity) {
			if (response.getFirstHeader(CONTENT_ENCODING)?.value == "gzip") {
				map.body = GzipEncoder.decode(response.entity.content)
			} else {
				map.body = response.entity.content.text
			}
		}
		map
	}

	protected ProtocolVersion parseProtocol(String protocolString) {
		def matcher = protocolString =~ /^(\w+)\/(\d+)\.(\d+)$/
		new ProtocolVersion(matcher[0][1], matcher[0][2].toInteger(), matcher[0][3].toInteger())
	}

}
