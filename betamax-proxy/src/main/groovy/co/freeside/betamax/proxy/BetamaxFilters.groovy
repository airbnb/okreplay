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

package co.freeside.betamax.proxy

import java.util.logging.Logger
import co.freeside.betamax.encoding.*
import co.freeside.betamax.message.Response
import co.freeside.betamax.proxy.netty.*
import co.freeside.betamax.tape.Tape
import com.google.common.base.Optional
import io.netty.buffer.*
import io.netty.handler.codec.http.*
import org.littleshoot.proxy.HttpFiltersAdapter
import static co.freeside.betamax.Headers.*
import static io.netty.handler.codec.http.HttpHeaders.Names.*
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1

class BetamaxFilters extends HttpFiltersAdapter {

	private NettyRequestAdapter request
	private NettyResponseAdapter upstreamResponse
	private final Tape tape

	private static final Logger log = Logger.getLogger(BetamaxFilters.class.getName());

	BetamaxFilters(HttpRequest originalRequest, Tape tape) {
		super(originalRequest)
		log.info "Created filter for $originalRequest.method $originalRequest.uri"
		request = NettyRequestAdapter.wrap(originalRequest)
		this.tape = tape
	}

	@Override
	HttpResponse requestPre(HttpObject httpObject) {
		log.info "requestPre ${httpObject.getClass().simpleName}"

		HttpResponse response = null

		if (httpObject instanceof HttpRequest) {
            request.copyHeaders(httpObject)
			response = onRequestIntercepted(httpObject).orNull()
		}

		return response
	}

	@Override
	HttpResponse requestPost(HttpObject httpObject) {
		log.info "requestPost ${httpObject.getClass().simpleName}"

		if (httpObject instanceof HttpContent) {
			request.append(httpObject)
		}

		if (httpObject instanceof HttpRequest) {
			setViaHeader(httpObject)
		}

		return null;
	}

	@Override
	void responsePre(HttpObject httpObject) {
		log.info "responsePre ${httpObject.getClass().simpleName}"

		if (httpObject instanceof HttpResponse) {
			upstreamResponse = NettyResponseAdapter.wrap(httpObject)

			// TODO: prevent this from getting written to tape
			setBetamaxHeader(httpObject, "REC");
		}

		if (httpObject instanceof HttpContent) {
			upstreamResponse.append(httpObject)
		}

		if (httpObject instanceof LastHttpContent) {
			log.warning("Recording to tape ${tape.name}")
			tape.record(request, upstreamResponse)
		}
	}

	@Override
	void responsePost(HttpObject httpObject) {
		log.info "responsePost ${httpObject.getClass().simpleName}"
		if (httpObject instanceof HttpResponse) {
			setViaHeader(httpObject)
		}
	}

	private Optional<FullHttpResponse> onRequestIntercepted(HttpRequest httpObject) {
        if (!tape) {
            return Optional.of(new DefaultFullHttpResponse(HTTP_1_1, new HttpResponseStatus(403, "No tape")))
        } else if (tape.isReadable() && tape.seek(request)) {
			def recordedResponse = tape.play(request)
			FullHttpResponse response = playRecordedResponse(recordedResponse)
			setViaHeader(response)
			setBetamaxHeader(response, "PLAY")
			return Optional.of(response)
		} else {
			log.warning "no matching request found on $tape.name"
			return Optional.absent()
		}
	}

	private DefaultFullHttpResponse playRecordedResponse(Response recordedResponse) {
		DefaultFullHttpResponse response
		def status = HttpResponseStatus.valueOf(recordedResponse.status)
		if (recordedResponse.hasBody()) {
			def content = getEncodedContent(recordedResponse)
			response = new DefaultFullHttpResponse(HTTP_1_1, status, content)
		} else {
			response = new DefaultFullHttpResponse(HTTP_1_1, status)
		}
		for (Map.Entry<String, String> header : recordedResponse.headers) {
			response.headers().set(header.key, header.value.split(/,\s*/).toList())
		}
		response
	}

	private ByteBuf getEncodedContent(Response recordedResponse) {
		byte[] stream
		switch (recordedResponse.getHeader(CONTENT_ENCODING)) {
			case "gzip":
				stream = new GzipEncoder().encode(recordedResponse.bodyAsBinary.bytes)
				break
			case "deflate":
				stream = new DeflateEncoder().encode(recordedResponse.bodyAsBinary.bytes)
				break
			default:
				stream = recordedResponse.bodyAsBinary.bytes
		}
		Unpooled.wrappedBuffer(stream)
	}

	private HttpHeaders setViaHeader(HttpMessage httpMessage) {
		httpMessage.headers().set(VIA, VIA_HEADER)
	}

	private HttpHeaders setBetamaxHeader(HttpResponse response, String value) {
		response.headers().add(X_BETAMAX, value)
	}
}
