package betamax

import betamax.util.EchoServer
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.junit.Rule
import static betamax.server.HttpProxyHandler.X_BETAMAX
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import spock.lang.*

@Stepwise
class AnnotationSpec extends Specification {

    @Rule Recorder recorder = Recorder.instance
    @AutoCleanup("stop") EchoServer endpoint = new EchoServer()
    RESTClient http

    def setupSpec() {
        Recorder.instance.tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
    }

    def setup() {
        http = new RESTClient(endpoint.url)
        http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
    }

    def cleanupSpec() {
        assert Recorder.instance.tapeRoot.deleteDir()
    }

    def "no tape is inserted if there is no annotation on the feature"() {
        expect:
        recorder.tape == null
    }

    @Betamax(tape = "annotation_spec")
    def "annotation on feature causes tape to be inserted"() {
        expect:
        recorder.tape.name == "annotation_spec"
    }

    def "tape is ejected after annotated feature completes"() {
        expect:
        recorder.tape == null
    }

    @Betamax(tape = "annotation_spec")
    def "annotated feature can record"() {
        given:
        endpoint.start()

        when:
        def response = http.get(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
        response.getFirstHeader(X_BETAMAX)?.value == "REC"
    }

    @Betamax(tape = "annotation_spec")
    def "annotated feature can play back"() {
        when:
        def response = http.get(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
        response.getFirstHeader(X_BETAMAX)?.value == "PLAY"
    }

    def "can make unproxied request after using annotation"() {
        given:
        endpoint.start()

        when:
        def response = http.get(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA) == null
    }

}
