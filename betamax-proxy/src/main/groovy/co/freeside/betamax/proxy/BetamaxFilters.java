/*
 * Copyright 2013 the original author or authors.
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

package co.freeside.betamax.proxy;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import co.freeside.betamax.encoding.*;
import co.freeside.betamax.message.*;
import co.freeside.betamax.proxy.netty.*;
import co.freeside.betamax.tape.*;
import com.google.common.base.*;
import com.google.common.io.*;
import io.netty.buffer.*;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.*;
import static co.freeside.betamax.Headers.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import static java.util.logging.Level.*;

public class BetamaxFilters extends HttpFiltersAdapter {

    private NettyRequestAdapter request;
    private NettyResponseAdapter upstreamResponse;
    private final Tape tape;

    private static final Logger LOG = Logger.getLogger(BetamaxFilters.class.getName());

    public BetamaxFilters(HttpRequest originalRequest, Tape tape) {
        super(originalRequest);
        LOG.info(String.format("Created filter for %s %s", originalRequest.getMethod(), originalRequest.getUri()));
        request = NettyRequestAdapter.wrap(originalRequest);
        this.tape = tape;
    }

    @Override
    public HttpResponse requestPre(HttpObject httpObject) {
        LOG.info(String.format("requestPre %s", httpObject.getClass().getSimpleName()));

        try {
            HttpResponse response = null;
            if (httpObject instanceof HttpRequest) {
                request.copyHeaders((HttpMessage) httpObject);
                response = onRequestIntercepted((HttpRequest) httpObject).orNull();
            }
            return response;
        } catch (IOException e) {
            return createErrorResponse(e);

        }
    }

    @Override
    public HttpResponse requestPost(HttpObject httpObject) {
        LOG.info(String.format("requestPost %s", httpObject.getClass().getSimpleName()));

        try {
            if (httpObject instanceof HttpContent) {
                request.append((HttpContent) httpObject);
            }

            if (httpObject instanceof HttpRequest) {
                setViaHeader((HttpMessage) httpObject);
            }

            return null;
        } catch (IOException e) {
            return createErrorResponse(e);
        }
    }

    @Override
    public void responsePre(HttpObject httpObject) {
        LOG.info(String.format("responsePre %s", httpObject.getClass().getSimpleName()));

        if (httpObject instanceof HttpResponse) {
            upstreamResponse = NettyResponseAdapter.wrap(httpObject);

            // TODO: prevent this from getting written to tape
            setBetamaxHeader((HttpResponse) httpObject, "REC");
        }

        if (httpObject instanceof HttpContent) {
            try {
                upstreamResponse.append((HttpContent) httpObject);
            } catch (IOException e) {
                // TODO: handle in some way
                LOG.log(SEVERE, "Error appending content", e);
            }
        }

        if (httpObject instanceof LastHttpContent) {
            LOG.warning(String.format("Recording to tape %s", tape.getName()));
            tape.record(request, upstreamResponse);
        }
    }

    @Override
    public void responsePost(HttpObject httpObject) {
        LOG.info(String.format("responsePost %s", httpObject.getClass().getSimpleName()));
        if (httpObject instanceof HttpResponse) {
            setViaHeader((HttpMessage) httpObject);
        }
    }

    private Optional<? extends FullHttpResponse> onRequestIntercepted(HttpRequest httpObject) throws IOException {
        if (tape == null) {
            return Optional.of(new DefaultFullHttpResponse(HTTP_1_1, new HttpResponseStatus(403, "No tape")));
        } else if (tape.isReadable() && tape.seek(request)) {
            Response recordedResponse = tape.play(request);
            FullHttpResponse response = playRecordedResponse(recordedResponse);
            setViaHeader(response);
            setBetamaxHeader(response, "PLAY");
            return Optional.of(response);
        } else {
            LOG.warning(String.format("no matching request found on %s", tape.getName()));
            return Optional.absent();
        }
    }

    private DefaultFullHttpResponse playRecordedResponse(Response recordedResponse) throws IOException {
        DefaultFullHttpResponse response;
        HttpResponseStatus status = HttpResponseStatus.valueOf(recordedResponse.getStatus());
        if (recordedResponse.hasBody()) {
            ByteBuf content = getEncodedContent(recordedResponse);
            response = new DefaultFullHttpResponse(HTTP_1_1, status, content);
        } else {
            response = new DefaultFullHttpResponse(HTTP_1_1, status);
        }
        for (Map.Entry<String, String> header : recordedResponse.getHeaders().entrySet()) {
            response.headers().set(header.getKey(), Arrays.asList(header.getValue().split(",\\s*")));
        }
        return response;
    }

    private ByteBuf getEncodedContent(Response recordedResponse) throws IOException {
        byte[] stream;
        String encodingHeader = recordedResponse.getHeader(CONTENT_ENCODING);
        if ("gzip".equals(encodingHeader)) {
            stream = new GzipEncoder().encode(ByteStreams.toByteArray(recordedResponse.getBodyAsBinary()));
        } else if ("deflate".equals(encodingHeader)) {
            stream = new DeflateEncoder().encode(ByteStreams.toByteArray(recordedResponse.getBodyAsBinary()));
        } else {
            stream = ByteStreams.toByteArray(recordedResponse.getBodyAsBinary());
        }
        return Unpooled.wrappedBuffer(stream);
    }

    private HttpHeaders setViaHeader(HttpMessage httpMessage) {
        return httpMessage.headers().set(VIA, VIA_HEADER);
    }

    private HttpHeaders setBetamaxHeader(HttpResponse response, String value) {
        return response.headers().add(X_BETAMAX, value);
    }

    private HttpResponse createErrorResponse(Throwable e) {
        // TODO: more detail
        return new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
    }

}
