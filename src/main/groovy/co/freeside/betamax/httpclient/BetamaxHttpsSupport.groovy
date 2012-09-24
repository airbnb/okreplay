package co.freeside.betamax.httpclient

import co.freeside.betamax.ssl.DummySSLSocketFactory
import org.apache.http.client.HttpClient
import org.apache.http.conn.scheme.Scheme

class BetamaxHttpsSupport {

    static void configure(HttpClient httpClient) {
        def httpsScheme = new Scheme('https', DummySSLSocketFactory.instance, 443)
		httpClient.connectionManager.schemeRegistry.register(httpsScheme)
    }

    private BetamaxHttpsSupport() {}

}
