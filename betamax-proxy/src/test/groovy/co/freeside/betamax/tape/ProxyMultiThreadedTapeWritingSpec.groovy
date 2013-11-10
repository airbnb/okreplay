package co.freeside.betamax.tape

import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.junit.RecorderRule
import org.junit.Rule

class ProxyMultiThreadedTapeWritingSpec extends MultiThreadedTapeWritingSpec {

    def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).build()
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    @Override
    protected String makeRequest(String url) {
        url.toURL().text
    }
}
