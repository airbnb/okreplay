package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@Unroll
class RequestMethodsSpec extends Specification {

    @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
    @Rule Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot)
    @Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

    void setupSpec() {
        endpoint.start(EchoHandler)
    }

    @Timeout(10)
	@Betamax(tape = 'proxy network comms spec', mode = TapeMode.READ_WRITE)
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
