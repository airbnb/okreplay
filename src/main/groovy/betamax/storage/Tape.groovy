package betamax.storage

import betamax.Betamax
import groovy.json.JsonBuilder
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.*
import org.apache.http.message.*

class Tape {

	String name
	Collection<HttpInteraction> interactions = []

	boolean play(HttpRequest request, HttpResponse response) {
		def interaction = interactions.find { it.request.requestLine.uri == request.requestLine.uri }
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
		interactions << new HttpInteraction(request, response)
	}

	void eject() {
		def json = new JsonBuilder()
		json.tape {
			name(name)
			interactions(interactions*.toMap())
		}
		file.parentFile.mkdirs()
		file.withWriter {writer ->
			writer << json.toPrettyString()
		}
	}

	File getFile() {
		new File(Betamax.instance.tapeRoot, "${name}.json")
	}

}

class HttpInteraction {

	final HttpRequest request
	final HttpResponse response

	HttpInteraction(HttpRequest request, HttpResponse response) {
		this.request = cloneRequest(request)
		this.response = cloneResponse(response)
	}

	Map toMap() {
		[request: requestToMap(), response: responseToMap()]
	}

	private Map requestToMap() {
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

	private Map responseToMap() {
		[
				protocol: response.statusLine.protocolVersion,
				status: response.statusLine.statusCode,
				body: response.entity.content.text
		]
	}

	private static HttpEntityEnclosingRequest cloneRequest(HttpEntityEnclosingRequest request) {
		def clone = new BasicHttpEntityEnclosingRequest(request.requestLine)
		clone.entity = cloneEntity(request.entity)
		clone
	}

	private static HttpRequest cloneRequest(HttpRequest request) {
		new BasicHttpRequest(request.requestLine)
	}

	private static HttpResponse cloneResponse(HttpResponse response) {
		def clone = new BasicHttpResponse(response.statusLine)
		clone.headers = response.allHeaders
		clone.entity = cloneEntity(response.entity)
		clone
	}

	private static HttpEntity cloneEntity(HttpEntity entity) {
		def bytes = new ByteArrayOutputStream()
		entity.writeTo(bytes)
		new ByteArrayEntity(bytes.toByteArray())
	}

}
