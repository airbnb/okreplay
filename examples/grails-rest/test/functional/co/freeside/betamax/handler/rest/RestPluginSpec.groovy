package co.freeside.betamax.handler.rest

import javax.servlet.http.*
import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import groovyx.net.http.*
import org.codehaus.groovy.grails.web.json.JSONObject
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.junit.Rule
import spock.lang.*
import static java.net.HttpURLConnection.HTTP_OK

class RestPluginSpec extends Specification {

	@Rule Recorder recorder = new Recorder(tapeRoot: new File('test/resources/tapes'), ignoreLocalhost: false)

	def http = new RESTClient('http://localhost:8080/grails-rest/')

	@Betamax(tape = 'rest')
	void 'can record data from rest plugin'() {
		given:
		def endpoint = new SimpleServer(5000)
		endpoint.start(EchoHandler)

		when:
		HttpResponseDecorator response = http.get(path: '/')

		then: 'embedded request went via Betamax'
		response.status == 200
		response.data.headers['Via'] == 'Betamax'

		and: 'this request did not'
		!response.getHeaders('Via')
		!response.getHeaders('X-Betamax')
	}

}

class EchoHandler extends AbstractHandler {

	@Override
	void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		response.status = HTTP_OK
		response.contentType = 'application/json'

		def json = new JSONObject()
		json.url = request.requestURI
		json.headers = request.headerNames.inject(new JSONObject()) { headers, headerName ->
			headers[headerName] = request.getHeader(headerName)
			headers
		}

		response.writer.withWriter { writer ->
			writer << json.toString()
		}
	}

}
