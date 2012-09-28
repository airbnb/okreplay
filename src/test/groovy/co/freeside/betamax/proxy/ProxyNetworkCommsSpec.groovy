package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.*
import org.apache.commons.httpclient.*
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.http.HttpHost
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY

@Unroll
class ProxyNetworkCommsSpec extends Specification {

    @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
    @Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
    @Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

    void setupSpec() {
        endpoint.start(EchoHandler)
    }

    @Timeout(10)
	@Betamax(tape = 'proxy network comms spec')
    void 'proxy intercepts URL connections'() {
        given:
        HttpURLConnection connection = new URL(endpoint.url).openConnection()
        connection.connect()

        expect:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == 'Betamax'

        cleanup:
        connection.disconnect()
    }

    @Timeout(10)
	@Betamax(tape = 'proxy network comms spec')
    void 'proxy intercepts HTTPClient connections when using ProxySelectorRoutePlanner'() {
        given:
        def http = new RESTClient(endpoint.url)
		BetamaxRoutePlanner.configure(http.client)

        when:
        def response = http.get(path: '/')

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == 'Betamax'
    }

    @Timeout(10)
	@Betamax(tape = 'proxy network comms spec')
    void 'proxy intercepts HTTPClient connections when explicitly told to'() {
        given:
        def http = new RESTClient(endpoint.url)
        http.client.params.setParameter(DEFAULT_PROXY, new HttpHost('localhost', recorder.proxyPort, 'http'))

        when:
        def response = http.get(path: '/')

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == 'Betamax'
    }

    @Timeout(10)
	@Betamax(tape = 'proxy network comms spec')
    void 'proxy intercepts HttpURLClient connections'() {
        given:
        def http = new HttpURLClient(url: endpoint.url)

        when:
        def response = http.request(path: '/')

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == 'Betamax'
    }

    @Timeout(10)
	@Betamax(tape = 'proxy network comms spec')
    void 'proxy intercepts HTTPClient 3.x connections'() {
        given:
        def client = new HttpClient()
		client.hostConfiguration.proxyHost = new ProxyHost('localhost', 5555)
		
		and:
        def request = new GetMethod(endpoint.url)

        when:
        def status = client.executeMethod(request)

        then:
        status == HTTP_OK
        request.getResponseHeader(VIA)?.value == 'Betamax'
    }

    @Timeout(10)
	@Betamax(tape = 'proxy network comms spec')
    void 'proxy handles #method requests'() {
        given:
        def http = new RESTClient(endpoint.url)
		BetamaxRoutePlanner.configure(http.client)

        when:
        def response = http."$method"(path: '/')

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == 'Betamax'

        cleanup:
        http.shutdown()

        where:
        method << ['get', 'post', 'put', 'head', 'delete', 'options']
    }

}
