package co.freeside.betamax.handler;

import co.freeside.betamax.message.Request;
import co.freeside.betamax.message.Response;
import co.freeside.betamax.message.httpclient.HttpResponseAdapter;
import com.google.common.io.ByteStreams;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.message.BasicHttpResponse;

import static co.freeside.betamax.Headers.VIA_HEADER;
import static org.apache.http.HttpHeaders.VIA;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;

public class TargetConnector implements HttpHandler {
    public TargetConnector(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Response handle(Request request) {
        HttpRequest outboundRequest = createOutboundRequest(request);
        HttpHost httpHost = new HttpHost(request.getUri().getHost(), request.getUri().getPort(), request.getUri().getScheme());

        try {
            HttpResponse response = httpClient.execute(httpHost, outboundRequest);
            return new HttpResponseAdapter(response);
        } catch (SocketTimeoutException e) {
            throw new TargetTimeoutException(request.getUri(), e);
        } catch (IOException e) {
            throw new TargetErrorException(request.getUri(), e);
        }
    }

    private HttpRequest createOutboundRequest(Request request) {
        final HttpRequest outboundRequest;
        try {
            outboundRequest = httpRequestFactory.newHttpRequest(request.getMethod(), request.getUri().toString());

            for (Map.Entry<String, String> header : request.getHeaders().entrySet())
                outboundRequest.addHeader(header.getKey(), header.getValue());

            outboundRequest.addHeader(VIA, VIA_HEADER);

            if (outboundRequest instanceof HttpEntityEnclosingRequest && request.hasBody())
                ((HttpEntityEnclosingRequest)(outboundRequest)).setEntity(new ByteArrayEntity(ByteStreams.toByteArray(request.getBodyAsBinary())));

            return outboundRequest;
        } catch (MethodNotSupportedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final HttpClient httpClient;
    private final HttpRequestFactory httpRequestFactory = new DefaultHttpRequestFactory();
}
