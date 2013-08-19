package co.freeside.betamax.message.filtering;

import co.freeside.betamax.message.Message;
import static org.apache.http.HttpHeaders.*;

import java.util.*;

public abstract class HeaderFilteringMessage implements Message {
    protected abstract Message getDelegate();

    public static final String PROXY_CONNECTION = "Proxy-Connection";
    public static final String KEEP_ALIVE = "Keep-Alive";
    public static final List<String> NO_PASS_HEADERS = new ArrayList<String>(Arrays.asList(CONTENT_LENGTH, HOST, PROXY_CONNECTION, CONNECTION, KEEP_ALIVE, PROXY_AUTHENTICATE, PROXY_AUTHORIZATION, TE, TRAILER, TRANSFER_ENCODING, UPGRADE));

    public final Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>(getDelegate().getHeaders());
        for (String headerName : NO_PASS_HEADERS)
            headers.remove(headerName);

        return headers;
    }

    public final String getHeader(String name) {
        return NO_PASS_HEADERS.contains(name) ? null : getDelegate().getHeader(name);
    }
}
