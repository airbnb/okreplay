package co.freeside.betamax.proxy.matchRules

import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.Recorder
import co.freeside.betamax.TapeMode
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.net.ssl.HttpsURLConnection

/**
 * Testing a custom matcher when being used in the proxy.
 */
@Unroll
class CustomMatcherSpec extends Specification {
    @Shared def tapeRoot = new File("src/test/resources/betamax/tapes")

    void "Using a custom matcher, can replay the tape"() {
        given:
        def imr = new InstrumentedMatchRule()
        def proxyConfig = ProxyConfiguration.builder()
                .sslEnabled(true)
                .tapeRoot(tapeRoot)
                .defaultMode(TapeMode.READ_WRITE)
                .defaultMatchRule(imr)
                .build()

        def recorder = new Recorder(proxyConfig)
        recorder.start("httpBinTape")

        when:
        def payload = "BUTTS"
        HttpsURLConnection conn = new URL("https://httpbin.org/post").openConnection()
        conn.setDoOutput(true)
        conn.setRequestMethod("POST")
        conn.setFixedLengthStreamingMode(payload.getBytes().length)
        def out = new PrintWriter(conn.getOutputStream())
        out.print(payload)
        out.close()

        then:
        def response = conn.getInputStream().getText()
        conn.disconnect()
        recorder.stop()

        response == "Hey look some text: BUTTS"
    }

    void "When the tape only contains a single entry in #mode, it should only match once"() {
        given:
        def imr = new InstrumentedMatchRule()
        def proxyConfig = ProxyConfiguration.builder()
                .sslEnabled(true)
                .tapeRoot(tapeRoot)
                .defaultMode(mode)
                .defaultMatchRule(imr)
                .build()

        def recorder = new Recorder(proxyConfig)
        recorder.start("httpBinTape")

        when:
        assert(imr.counter.get() == 0)
        def payload = "BUTTS"
        HttpsURLConnection conn = new URL("https://httpbin.org/post").openConnection()
        conn.setDoOutput(true)
        conn.setRequestMethod("POST")
        conn.setFixedLengthStreamingMode(payload.getBytes().length)
        def out = new PrintWriter(conn.getOutputStream())
        out.print(payload)
        out.close()

        then:
        def response = conn.getInputStream().getText()
        conn.disconnect()
        recorder.stop()

        imr.counter.get() == 1
        response == "Hey look some text: BUTTS"

        where:
        mode << [TapeMode.READ_ONLY, TapeMode.READ_WRITE]
    }



}
