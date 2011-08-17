package betamax.storage

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

	}

}

class HttpInteraction {

	final HttpRequest request
	final HttpResponse response

	HttpInteraction(HttpRequest request, HttpResponse response) {
		this.request = new BasicHttpRequest(request.requestLine)
		this.response = cloneResponse(response)
	}

	HttpInteraction(HttpEntityEnclosingRequest request, HttpResponse response) {
		this.request = new BasicHttpEntityEnclosingRequest(request.requestLine)
		this.request.entity = cloneEntity(request.entity)
		this.response = cloneResponse(response)
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
