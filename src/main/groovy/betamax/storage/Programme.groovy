package betamax.storage

import org.apache.http.HttpResponse
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.ProtocolVersion
import org.apache.http.HttpRequest

class Programme {

    Programme() {
        request = new Request()
        response = new Response()
    }

    final Request request
    final Response response

	static Programme write(HttpRequest httpRequest, HttpResponse httpResponse) {
		def programme = new Programme()
		programme.request.method = httpRequest.requestLine.method
		programme.request.uri = httpRequest.requestLine.uri
		programme.response.protocol = httpResponse.statusLine.protocolVersion.toString()
		programme.response.status = httpResponse.statusLine.statusCode
		programme.response.body = httpResponse.entity.content.text
		programme.response.headers = httpResponse.allHeaders.collectEntries {
			[it.name, it.value]
		}
		programme
	}

	void readTo(HttpResponse httpResponse) {
		httpResponse.setStatusLine(parseProtocol(response.protocol), response.status)
		httpResponse.entity = new BasicHttpEntity()
		httpResponse.entity.content = new ByteArrayInputStream(response.body.bytes)
		httpResponse.entity.contentLength = response.body.length()
		response.headers.each {
			httpResponse.addHeader(it.key, it.value)
		}
	}

	private ProtocolVersion parseProtocol(String protocol) {
		def matcher = protocol =~ /^(\w+)\/(\d+)\.(\d+)$/
		return new ProtocolVersion(matcher[0][1], matcher[0][2].toInteger(), matcher[0][3].toInteger())
	}

}

class Request {
    String method
    String uri
}

class Response {
	String protocol
	int status
	String body
	Map<String, String> headers = [:]
}