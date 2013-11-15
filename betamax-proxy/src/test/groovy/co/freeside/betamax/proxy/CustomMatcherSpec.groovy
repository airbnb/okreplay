package co.freeside.betamax.proxy

import co.freeside.betamax.MatchRule
import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.Recorder
import co.freeside.betamax.TapeMode
import co.freeside.betamax.message.Request
import spock.lang.Shared
import spock.lang.Specification

import javax.net.ssl.HttpsURLConnection

/**
 * Created by dkowis on 11/14/13.
 */
class CustomMatcherSpec extends Specification {
    @Shared def tapeRoot = new File("src/test/resources/betamax/tapes")
    @Shared def customMatchRule = new MatchRule() {
        @Override
        boolean isMatch(Request a, Request b) {
            if(a.getUri() == b.getUri() && a.getMethod() == b.getMethod()) {
                //Same method and URI, lets do a body comparison
                def aBody = a.getBodyAsText().getInput().getText()
                def bBody = b.getBodyAsText().getInput().getText()

                //Ideally in the real world, we'd parse the XML or the JSON and compare the ASTs instead
                // of just comparing the body strings, so that meaningless whitespace doesn't mean anything
                System.err.println("aBody: " + aBody)
                System.err.println("bBody: " + bBody)

                //Right now, lets just compare the bodies also
                return aBody.equals(bBody)
            } else {
                //URI and method don't match, so we're going to bail
                return false
            }
        }
    }


    void "Using a custom matcher, can replay the tape"() {
        given:
        def proxyConfig = ProxyConfiguration.builder()
                .sslEnabled(true)
                .tapeRoot(tapeRoot)
                .defaultMode(TapeMode.READ_ONLY)
                .defaultMatchRule(customMatchRule)
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

        response == "Hey look some text: BUTTS"

    }



}
