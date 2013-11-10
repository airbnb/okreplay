package co.freeside.betamax.tape

import co.freeside.betamax.Configuration
import co.freeside.betamax.httpclient.BetamaxHttpClient
import co.freeside.betamax.junit.RecorderRule
import org.apache.http.client.methods.HttpGet
import org.junit.Rule

class HttpClientMultiThreadedTapeWritingSpec extends MultiThreadedTapeWritingSpec {

    def configuration = Configuration.builder().tapeRoot(tapeRoot).build()
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    def httpClient = new BetamaxHttpClient(configuration, recorder)

    protected String makeRequest(String url) {
        def request = new HttpGet(url)
        def response = httpClient.execute(request)
        response.entity.content.text
    }
}
