package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.*
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.*
import spock.lang.*
import static java.net.HttpURLConnection.HTTP_FORBIDDEN

@Issue('https://github.com/robfletcher/betamax/issues/18')
class NoTapeSpec extends Specification {

	@Shared Recorder recorder = new ProxyRecorder()
	@Shared @AutoCleanup('stop') ProxyServer proxy = new ProxyServer(recorder)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	RESTClient http = new BetamaxRESTClient(endpoint.url)

	void setupSpec() {
		proxy.start()
		endpoint.start(EchoHandler)
	}

	void 'an error is returned if the proxy intercepts a request when no tape is inserted'() {
		when:
		http.get(path: '/')

		then:
		def e = thrown(HttpResponseException)
		e.statusCode == HTTP_FORBIDDEN
		e.message == 'No tape'
	}
}
