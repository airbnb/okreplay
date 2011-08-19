package betamax.storage

import org.apache.http.entity.ByteArrayEntity
import org.apache.http.*
import org.apache.http.message.*

class Tape {

	String name
	String description
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
		interactions << new HttpInteraction(request: cloneRequest(request), response: cloneResponse(response), recorded: new Date())
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

class HttpInteraction {

	HttpRequest request
	HttpResponse response
	String description
	Date recorded

}
