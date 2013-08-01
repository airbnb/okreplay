package co.freeside.betamax.httpclient

import org.apache.http.client.HttpClient
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner

/**
 * A convenience extension of ProxySelectorRoutePlanner that will configure proxy selection in a way that will work with
 * Betamax.
 */
class BetamaxRoutePlanner extends ProxySelectorRoutePlanner {

    static void configure(AbstractHttpClient httpClient) {
        def routePlanner = new BetamaxRoutePlanner(httpClient)
		httpClient.routePlanner = routePlanner
    }

    BetamaxRoutePlanner(HttpClient httpClient) {
        this(httpClient.connectionManager.schemeRegistry)
    }

    BetamaxRoutePlanner(SchemeRegistry schemeRegistry) {
        super(schemeRegistry, ProxySelector.default)
    }
}
