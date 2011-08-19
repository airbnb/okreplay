package betamax.storage

import groovy.json.JsonBuilder
import java.text.SimpleDateFormat
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.*
import org.apache.http.message.*
import groovy.json.JsonSlurper

class Tape implements Writable {

	String name
	String description
	Collection<HttpInteraction> interactions = []

	Tape(String name) {
		this.name = name
	}

	Tape(File file) {
		def json = file.withReader { reader ->
			new JsonSlurper().parse(reader)
		}

		name = json.tape.name
		json.tape.interactions.each {
			interactions << new HttpInteraction(it)
		}
	}

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

	Writer writeTo(Writer out) {
		def json = new JsonBuilder()
		json.tape {
			name(name)
			interactions(interactions*.toMap())
		}
		out << json.toPrettyString()
	}

}

class HttpInteraction {

	static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss Z"

	final HttpRequest request
	final HttpResponse response
	final String description
	final Date recorded

	HttpInteraction(HttpRequest request, HttpResponse response) {
		this.request = cloneRequest(request)
		this.response = cloneResponse(response)
		this.recorded = new Date()
	}

	HttpInteraction(json) {
		def requestProtocol = parseProtocol(json.request.protocol)
		def responseProtocol = parseProtocol(json.response.protocol)
		request = new BasicHttpRequest(json.request.method, json.request.uri, requestProtocol)
		response = new BasicHttpResponse(responseProtocol, json.response.status, null)
		recorded = new SimpleDateFormat(TIMESTAMP_FORMAT).parse(json.recorded)
	}

	private ProtocolVersion parseProtocol(String protocolString) {
		def matcher = protocolString =~ /^(\w+)\/(\d+)\.(\d+)$/
		new ProtocolVersion(matcher[0][1], matcher[0][2].toInteger(), matcher[0][3].toInteger())
	}

	Map toMap() {
		[recorded: new SimpleDateFormat(TIMESTAMP_FORMAT).format(recorded), request: requestToMap(), response: responseToMap()]
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
		def map = [
				protocol: response.statusLine.protocolVersion,
				status: response.statusLine.statusCode
		]
		if (response.entity) {
			map.body = response.entity.content.text
		}
		map
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
