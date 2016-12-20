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

package software.betamax.proxy;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.betamax.Headers;
import software.betamax.encoding.DeflateEncoder;
import software.betamax.encoding.GzipEncoder;
import software.betamax.handler.NonWritableTapeException;
import software.betamax.message.Response;
import software.betamax.proxy.netty.NettyRequestAdapter;
import software.betamax.proxy.netty.NettyResponseAdapter;
import software.betamax.tape.Tape;

import java.io.IOException;
import java.util.Map;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaders.Names.VIA;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class BetamaxFilters extends HttpFiltersAdapter {

    private NettyRequestAdapter request;
    private NettyResponseAdapter upstreamResponse;
    private final Tape tape;

    private static final Logger LOG = LoggerFactory.getLogger(BetamaxFilters.class.getName());

    public BetamaxFilters(HttpRequest originalRequest, Tape tape) {
        super(originalRequest);
        request = NettyRequestAdapter.wrap(originalRequest);
        this.tape = tape;
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        try {
            HttpResponse response = null;
            if (httpObject instanceof HttpRequest) {
                //TODO: I believe this is where the CONNECT needs to be caught...
                // This would require changing the predicate to include all things
                // As well, an appropriate response that the connect succeeded would have to be returned
                // But only if the server we are trying to connect to actually has an entry in the tape
                // It's something of a race condition with the SSL stuff. Because I don't believe we get a path
                // When we have the connect go through. Could we send the connect later if we didn't send it now?
                request.copyHeaders((HttpMessage) httpObject);
            }

            // If we're getting content stick it in there.
            if (httpObject instanceof HttpContent) {
                request.append((HttpContent) httpObject);
            }

            // If it's the last one, we want to take further steps, like checking to see if we've recorded on it!
            if (ProxyUtils.isLastChunk(httpObject)) {
                // We will have collected the last of the http Request finally
                // And now we're ready to intercept it and do proxy-type-things
                response = onRequestIntercepted().orNull();
            }

            if (response != null) {
                HttpHeaders.setKeepAlive(response, false);
            }

            return response;
        } catch (IOException e) {
            return createErrorResponse(e);
        }
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (httpObject instanceof HttpResponse) {
            upstreamResponse = NettyResponseAdapter.wrap(httpObject);
        }

        if (httpObject instanceof HttpContent) {
            try {
                upstreamResponse.append((HttpContent) httpObject);
            } catch (IOException e) {
                // TODO: handle in some way
                LOG.error("Error appending content", e);
            }
        }

        if (ProxyUtils.isLastChunk(httpObject)) {
            if (tape.isWritable()) {
                LOG.info(String.format("Recording to tape %s", tape.getName()));
                tape.record(request, upstreamResponse);
            } else {
                throw new NonWritableTapeException();
            }
        }

        return httpObject;
    }

    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
        if (httpObject instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) httpObject;

            // we're not recording if we're playing back
            if (response.headers().contains(Headers.X_BETAMAX)) {
                return response;
            }

            setBetamaxHeader(response, "REC");
            setViaHeader(response);
        }

        return httpObject;
    }

    private Optional<? extends FullHttpResponse> onRequestIntercepted() throws IOException {
        if (tape == null) {
            return Optional.of(new DefaultFullHttpResponse(HTTP_1_1, new HttpResponseStatus(403, "No tape")));
        } else if (tape.isReadable() && tape.seek(request)) {
            LOG.warn(String.format("Playing back from tape %s", tape.getName()));
            Response recordedResponse = tape.play(request);
            FullHttpResponse response = playRecordedResponse(recordedResponse);
            setViaHeader(response);
            setBetamaxHeader(response, "PLAY");
            return Optional.of(response);
        } else {
            LOG.warn(String.format("no matching request found on %s", tape.getName()));
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
            response.headers().set(header.getKey(), Splitter.onPattern(",\\s*").split(header.getValue()));
        }
        return response;
    }

    private ByteBuf getEncodedContent(Response recordedResponse) throws IOException {
        byte[] stream;
        String encodingHeader = recordedResponse.getHeader(CONTENT_ENCODING);
        if ("gzip".equals(encodingHeader)) {
            stream = new GzipEncoder().encode(recordedResponse.getBodyAsBinary());
        } else if ("deflate".equals(encodingHeader)) {
            stream = new DeflateEncoder().encode(recordedResponse.getBodyAsBinary());
        } else {
            stream = recordedResponse.getBodyAsBinary();
        }
        return wrappedBuffer(stream);
    }

    private HttpHeaders setViaHeader(HttpMessage httpMessage) {
        return httpMessage.headers().set(VIA, Headers.VIA_HEADER);
    }

    private HttpHeaders setBetamaxHeader(HttpResponse response, String value) {
        return response.headers().set(Headers.X_BETAMAX, value);
    }

    private HttpResponse createErrorResponse(Throwable e) {
        // TODO: more detail
        return new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
    }

}
