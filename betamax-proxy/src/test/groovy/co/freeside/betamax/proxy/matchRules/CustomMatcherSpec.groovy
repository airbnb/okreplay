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
}
