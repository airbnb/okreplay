package co.freeside.betamax.proxy.matchRules

import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.Recorder
import co.freeside.betamax.TapeMode
import com.google.common.io.Files
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

    void "Using a custom matcher it should record a new one"() {
        given:
        def tapeRoot = Files.createTempDir() //Using a temp dir this time
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
        def response = simplePost("https://httpbin.org/post", "LOLWUT")
        then:
        def content = response.toString()
        recorder.stop()
        //The tape is written when it's referenced not in this dir
        //make sure there's a file in there
        def recordedTape = new File(tapeRoot, "httpBinTape.yaml")
        //It should have recorded it to the tape
        recordedTape.exists()
    }

}
