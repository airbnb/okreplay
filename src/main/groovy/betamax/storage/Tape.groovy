package betamax.storage

import org.apache.http.entity.BasicHttpEntity
import org.apache.http.*

class Tape {

    String name
    Collection<Programme> programmes = new HashSet<Programme>()

    boolean read(HttpRequest request, HttpResponse response) {
		def programme = programmes.find { it.request.uri == request.requestLine.uri }
		if (programme) {
			response.setStatusLine(parseProtocol(programme.response.protocol), programme.response.status)
			response.entity = new BasicHttpEntity()
			response.entity.content = new ByteArrayInputStream(programme.response.body.bytes)
			response.entity.contentLength = programme.response.body.length()
			programme.response.headers.each {
				response.addHeader(it.key, it.value)
			}
			true
		} else {
			false
		}
    }

	void write(HttpRequest request, HttpResponse response) {
        def programme = new Programme()
        programme.request.method = request.requestLine.method
        programme.request.uri = request.requestLine.uri
		programme.response.protocol = response.statusLine.protocolVersion.toString()
        programme.response.status = response.statusLine.statusCode
        programme.response.body = response.entity.content.text
        programme.response.headers = response.allHeaders.collectEntries {
            [it.name, it.value]
        }
        programmes << programme
    }

    void eject() {

    }

	private ProtocolVersion parseProtocol(String protocol) {
		def matcher = protocol =~ /^(\w+)\/(\d+)\.(\d+)$/
		return new ProtocolVersion(matcher[0][1], matcher[0][2].toInteger(), matcher[0][3].toInteger())
	}

}
