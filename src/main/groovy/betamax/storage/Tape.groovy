package betamax.storage

import org.apache.http.*
import org.apache.http.message.BasicHttpResponse
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.message.BasicHttpRequest

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

		this.response = new BasicHttpResponse(response.statusLine)
		this.response.headers = response.allHeaders
		def bytes = new ByteArrayOutputStream()
		response.entity.writeTo(bytes)
		this.response.entity = new ByteArrayEntity(bytes.toByteArray())
	}
}
