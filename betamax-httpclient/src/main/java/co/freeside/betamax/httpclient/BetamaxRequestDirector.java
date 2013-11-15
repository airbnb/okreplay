/*
 * Copyright 2012 the original author or authors.
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

package co.freeside.betamax.httpclient;

import java.io.IOException;
import java.util.Map;
import co.freeside.betamax.*;
import co.freeside.betamax.handler.*;
import co.freeside.betamax.message.*;
import co.freeside.betamax.message.httpclient.HttpRequestAdapter;
import com.google.common.io.ByteStreams;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.entity.*;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import static java.util.Locale.ENGLISH;
import static org.apache.http.HttpVersion.HTTP_1_1;

class BetamaxRequestDirector implements RequestDirector {

    private final RequestDirector delegate;
    private final Configuration configuration;
    private final HttpHandler handlerChain;

    public BetamaxRequestDirector(final RequestDirector delegate, Configuration configuration, Recorder recorder, CredentialsProvider credentialsProvider) {
        this.delegate = delegate;
        this.configuration = configuration;

        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credentialsProvider)
                .useSystemProperties()
                .build();
        handlerChain = new DefaultHandlerChain(recorder, httpClient);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, HttpException {
        if (shouldIgnore(target)) {
            return delegate.execute(target, request, context);
        } else {
            return handleRequest(request);
        }
    }

    private HttpResponse handleRequest(HttpRequest request) throws IOException {
        Request requestWrapper = new HttpRequestAdapter(request);
        Response responseWrapper = handlerChain.handle(requestWrapper);

        HttpResponse response = new BasicHttpResponse(
                HTTP_1_1,
                responseWrapper.getStatus(),
                EnglishReasonPhraseCatalog.INSTANCE.getReason(responseWrapper.getStatus(), ENGLISH)
        );
        for (Map.Entry<String, String> header : responseWrapper.getHeaders().entrySet()) {
            for (String value : header.getValue().split(",")) {
                response.addHeader(header.getKey(), value.trim());
            }
        }
        if (responseWrapper.hasBody()) {
            response.setEntity(new ByteArrayEntity(ByteStreams.toByteArray(responseWrapper.getBodyAsBinary()), ContentType.create(responseWrapper.getContentType())));
        }
        return response;
    }

    private boolean shouldIgnore(HttpHost target) {
        return configuration.getIgnoreHosts().contains(target.getHostName());
    }
}
