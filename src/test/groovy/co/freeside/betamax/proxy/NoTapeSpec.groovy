package co.freeside.betamax.proxy

import co.freeside.betamax.Recorder
import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import co.freeside.betamax.proxy.jetty.ProxyServer
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import static java.net.HttpURLConnection.HTTP_FORBIDDEN

@Issue("https://github.com/robfletcher/betamax/issues/18")
class NoTapeSpec extends Specification {

	@Shared @AutoCleanup("restoreOriginalProxySettings") Recorder recorder = new Recorder()
	@Shared @AutoCleanup("stop") ProxyServer proxy = new ProxyServer()
	@Shared @AutoCleanup("stop") SimpleServer endpoint = new SimpleServer()
	RESTClient http

	def setupSpec() {
		proxy.start(recorder)
		recorder.overrideProxySettings()
		endpoint.start(EchoHandler)
	}

	def setup() {
		http = new RESTClient(endpoint.url)
		BetamaxRoutePlanner.configure(http.client)
	}

	def "an error is returned if the proxy intercepts a request when no tape is inserted"() {
		when:
		http.get(path: "/")

		then:
		def e = thrown(HttpResponseException)
		e.statusCode == HTTP_FORBIDDEN
		e.message == "No tape"
	}
}
