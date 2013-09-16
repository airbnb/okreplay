package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.*
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.RESTClient
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static org.apache.http.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/16')
@Unroll
class IgnoreHostsSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	@AutoCleanup('ejectTape') Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)
	@AutoCleanup('stop') ProxyServer proxy = new ProxyServer(recorder)
	RESTClient http

	void setupSpec() {
		endpoint.start(EchoHandler)
	}

	void setup() {
		http = new BetamaxRESTClient()

		recorder.insertTape('ignore hosts spec')
	}

	void 'does not proxy a request to #requestURI when ignoring #ignoreHosts'() {
		given: 'proxy is configured to ignore local connections'
		recorder.ignoreHosts = [ignoreHosts]
		proxy.start()

		when: 'a request is made'
		def response = http.get(uri: requestURI)

		then: 'the request is not intercepted by the proxy'
		!response.headers[VIA]

		and: 'nothing is recorded to the tape'
		recorder.tape.size() == old(recorder.tape.size())

		where:
		ignoreHosts               | requestURI
		endpoint.url.toURI().host | endpoint.url
		'localhost'               | "http://localhost:${endpoint.url.toURI().port}"
		'127.0.0.1'               | "http://localhost:${endpoint.url.toURI().port}"
		endpoint.url.toURI().host | "http://localhost:${endpoint.url.toURI().port}"
	}

	void 'does not proxy a request to #requestURI when ignoreLocalhost is true'() {
		given: 'proxy is configured to ignore local connections'
		recorder.ignoreLocalhost = true
		proxy.start()

		when: 'a request is made'
		def response = http.get(uri: requestURI)

		then: 'the request is not intercepted by the proxy'
		!response.headers[VIA]

		and: 'nothing is recorded to the tape'
		recorder.tape.size() == old(recorder.tape.size())

		where:
		requestURI << [endpoint.url, "http://localhost:${endpoint.url.toURI().port}"]
	}

}
