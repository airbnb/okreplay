package betamax

import betamax.util.EchoServer
import groovyx.net.http.HttpURLClient
import org.junit.Rule
import static betamax.server.HttpProxyHandler.X_BETAMAX
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import spock.lang.*

@Stepwise
class UsingRuleWithSpockSpec extends Specification {

    @Rule Recorder recorder = Recorder.instance
    @AutoCleanup("stop") EchoServer endpoint = new EchoServer()

    def setupSpec() {
        Recorder.instance.tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
    }

    def cleanupSpec() {
        assert Recorder.instance.tapeRoot.deleteDir()
    }

    def "no tape is inserted if there is no annotation on the test"() {
        expect:
        recorder.tape == null
    }

    @Betamax(tape = "annotation_test")
    def "annotation on test causes tape to be inserted"() {
        expect:
        recorder.tape.name == "annotation_test"
    }

    def "tape is ejected after annotated test completes"() {
        expect:
        recorder.tape == null
    }

    @Betamax(tape = "annotation_test")
    def "annotated test can record"() {
        given:
        endpoint.start()

        and:
        def http = new HttpURLClient(url: endpoint.url)

        when:
        def response = http.request(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
        response.getFirstHeader(X_BETAMAX)?.value == "REC"
    }

    @Betamax(tape = "annotation_test")
    def "annotated test can play back"() {
        given:
        def http = new HttpURLClient(url: endpoint.url)

        when:
        def response = http.request(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA)?.value == "Betamax"
        response.getFirstHeader(X_BETAMAX)?.value == "PLAY"
    }

    def "can make unproxied request after using annotation"() {
        given:
        endpoint.start()

        and:
        def http = new HttpURLClient(url: endpoint.url)

        when:
        def response = http.request(path: "/")

        then:
        response.status == HTTP_OK
        response.getFirstHeader(VIA) == null
    }

}
