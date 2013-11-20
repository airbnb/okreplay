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
    @Shared
    def tapeRoot = new File(CustomMatcherSpec.class.getResource("/betamax/tapes/").toURI())

    def simplePost(String url, String payload) {
        def output = null
        HttpsURLConnection conn = new URL(url).openConnection()
        conn.setDoOutput(true)
        conn.setRequestMethod("POST")
        conn.setFixedLengthStreamingMode(payload.getBytes().length)
        def out = new PrintWriter(conn.getOutputStream())
        out.print(payload)
        out.flush()
        out.close()

        output = conn.getInputStream().getText()
        conn.disconnect()

        return output
    }

    void "Using a custom matcher it should replay"() {
        given:
        def imr = new InstrumentedMatchRule()
        def proxyConfig = ProxyConfiguration.builder()
                .sslEnabled(true)
                .tapeRoot(tapeRoot)
                .defaultMode(TapeMode.READ_ONLY)
                .defaultMatchRule(imr)
                .build()

        def recorder = new Recorder(proxyConfig)
        recorder.start("httpBinTape")
        imr.requestValidations << { r ->
            //Will run this request validation on both requests being matched
            //No matter what, either recorded, or sent, I should have a payload of "BUTTS"
            //I'm posting "BUTTS" and the recorded interaction should have "BUTTS"
            if(!r.hasBody() ){
                println("REQUEST BODY WASNT THERE!!!")
            }
        }

        when:
        def response = simplePost("https://httpbin.org/post", "BUTTS")
        then:
        def content = response.toString()
        recorder.stop()

        content == "Hey look some text: BUTTS"
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
        assert (imr.counter.get() == 0)
        def response = simplePost("https://httpbin.org/post", "BUTTS")

        then:
        recorder.stop()

        imr.counter.get() == 1
        response == "Hey look some text: BUTTS"

        where:
        mode << [TapeMode.READ_ONLY, TapeMode.READ_WRITE]
    }


}
