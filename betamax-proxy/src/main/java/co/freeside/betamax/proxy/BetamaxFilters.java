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

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import co.freeside.betamax.encoding.*;
import co.freeside.betamax.handler.NonWritableTapeException;
import co.freeside.betamax.message.Response;
import co.freeside.betamax.proxy.netty.*;
import co.freeside.betamax.tape.Tape;
import com.google.common.base.*;
import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFiltersAdapter;

import static co.freeside.betamax.Headers.*;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.util.logging.Level.SEVERE;

public class BetamaxFilters extends HttpFiltersAdapter {

    private NettyRequestAdapter request;
    private NettyResponseAdapter upstreamResponse;
    private final Tape tape;

    private static final Logger LOG = Logger.getLogger(BetamaxFilters.class.getName());

    public BetamaxFilters(HttpRequest originalRequest, Tape tape) {
        super(originalRequest);
        request = NettyRequestAdapter.wrap(originalRequest);
        this.tape = tape;
    }

    @Override
    public HttpResponse requestPre(HttpObject httpObject) {
        try {
            HttpResponse response = null;
            if (httpObject instanceof HttpRequest) {
                request.copyHeaders((HttpMessage) httpObject);
            }

            //If we're getting content stick it in there.
            if (httpObject instanceof HttpContent) {
                request.append((HttpContent) httpObject);
                //If it's the last one, we want to take further steps, like checking to see if we've recorded on it!
                if (httpObject instanceof LastHttpContent) {
                    //We will have collected the last of the http Request finally
                    //And now we're ready to intercept it and do proxy-type-things
                    response = onRequestIntercepted().orNull();
                }
            }

            return response;
        } catch (IOException e) {
            return createErrorResponse(e);
        }
    }

    @Override
    public HttpResponse requestPost(HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            setViaHeader((HttpMessage) httpObject);
        }

        return null;
    }

    @Override
    public void responsePre(HttpObject httpObject) {
        if (httpObject instanceof HttpResponse) {
            upstreamResponse = NettyResponseAdapter.wrap(httpObject);
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
            if (tape.isWritable()) {
                LOG.info(String.format("Recording to tape %s", tape.getName()));
                tape.record(request, upstreamResponse);
            } else {
                throw new NonWritableTapeException();
            }
        }
    }

    @Override
    public void responsePost(HttpObject httpObject) {
        if (httpObject instanceof HttpResponse) {
            setBetamaxHeader((HttpResponse) httpObject, "REC");
            setViaHeader((HttpMessage) httpObject);
        }
    }

    private Optional<? extends FullHttpResponse> onRequestIntercepted() throws IOException {
        if (tape == null) {
            return Optional.of(new DefaultFullHttpResponse(HTTP_1_1, new HttpResponseStatus(403, "No tape")));
        } else if (tape.isReadable() && tape.seek(request)) {
            LOG.warning(String.format("Playing back from tape %s", tape.getName()));
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
            response.headers().set(header.getKey(), Splitter.onPattern(",\\s*").split(header.getValue()));
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
        return wrappedBuffer(stream);
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
