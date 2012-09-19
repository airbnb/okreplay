package co.freeside.betamax.httpclient;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;

import java.net.ProxySelector;

/**
 * A convenience extension of ProxySelectorRoutePlanner that will configure proxy selection in a way that will work with
 * Betamax.
 */
public class BetamaxRoutePlanner extends ProxySelectorRoutePlanner {

    public static void configure(AbstractHttpClient httpClient) {
        HttpRoutePlanner routePlanner = new BetamaxRoutePlanner(httpClient);
        httpClient.setRoutePlanner(routePlanner);
    }

    public BetamaxRoutePlanner(HttpClient httpClient) {
        this(httpClient.getConnectionManager().getSchemeRegistry());
    }

    public BetamaxRoutePlanner(SchemeRegistry schemeRegistry) {
        super(schemeRegistry, ProxySelector.getDefault());
    }
}
