/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.handler;

import java.io.*;
import java.net.*;
import java.util.*;
import co.freeside.betamax.message.*;
import co.freeside.betamax.message.httpclient.*;
import com.google.common.io.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.entity.*;
import org.apache.http.impl.*;
import static co.freeside.betamax.Headers.*;
import static org.apache.http.HttpHeaders.*;

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

            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                outboundRequest.addHeader(header.getKey(), header.getValue());
            }

            outboundRequest.addHeader(VIA, VIA_HEADER);

            if (outboundRequest instanceof HttpEntityEnclosingRequest && request.hasBody()) {
                ((HttpEntityEnclosingRequest) outboundRequest).setEntity(new ByteArrayEntity(ByteStreams.toByteArray(request.getBodyAsBinary())));
            }

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
