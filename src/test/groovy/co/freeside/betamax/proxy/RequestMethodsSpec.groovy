package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
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
class RequestMethodsSpec extends Specification {

    @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
    @Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
    @Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

    void setupSpec() {
        endpoint.start(EchoHandler)
    }

    @Timeout(10)
	@Betamax(tape = 'proxy network comms spec')
    void 'proxy handles #method requests'() {
        given:
        def http = new BetamaxRESTClient(endpoint.url)

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
