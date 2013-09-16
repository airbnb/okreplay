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
import co.freeside.betamax.handler.NonWritableTapeException
import co.freeside.betamax.message.Request
import co.freeside.betamax.proxy.netty.*
import co.freeside.betamax.tape.Tape
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.*
import org.littleshoot.proxy.HttpFiltersAdapter
import static co.freeside.betamax.Headers.*
import static io.netty.handler.codec.http.HttpHeaders.Names.VIA
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import static java.util.logging.Level.INFO

class BetamaxFilters extends HttpFiltersAdapter {

	private final Request request
	private NettyResponseAdapter upstreamResponse
	private final Tape tape
	private static final Logger log = Logger.getLogger(BetamaxFilters.class.getName());

	BetamaxFilters(HttpRequest originalRequest, Tape tape) {
		super(originalRequest)
		this.request = NettyRequestAdapter.wrap(originalRequest)
		this.tape = tape
	}

	@Override
	HttpResponse requestPre(HttpObject httpObject) {
		println "requestPre ${httpObject.getClass().name}"

		FullHttpResponse response = null

		if (httpObject instanceof HttpRequest) {
			log.info "checking tape..., $request.method, $request.uri on $tape.name ${tape.size()}"
			if (tape.isReadable() && tape.seek(request)) {
				log.info("Playing back from '" + tape.getName() + "'");
				def recordedResponse = tape.play(request)
				def status = HttpResponseStatus.valueOf(recordedResponse.status)
				def content = recordedResponse.hasBody() ? Unpooled.copiedBuffer(recordedResponse.bodyAsBinary.bytes) : Unpooled.EMPTY_BUFFER
				response = recordedResponse.hasBody() ? new DefaultFullHttpResponse(HTTP_1_1, status, content) : new DefaultFullHttpResponse(HTTP_1_1, status)
				for (Map.Entry<String, String> header : recordedResponse.headers) {
					response.headers().set(header.key, header.value.split(/,\s*/).toList())
				}

				response.headers().add(VIA, "Betamax")
				response.headers().add(X_BETAMAX, "PLAY")
			} else {
				log.warning "not found"
			}
		}

		return response
	}

	@Override
	HttpResponse requestPost(HttpObject httpObject) {
		println "requestPost ${httpObject.getClass().name}"

		if (httpObject instanceof HttpRequest) {
			((HttpRequest) httpObject).headers().set(VIA, "Betamax")
		}

		return null;
	}

	@Override
	void responsePre(HttpObject httpObject) {
		println "responsePre ${httpObject.getClass().name}"

		if (httpObject instanceof HttpResponse) {
			upstreamResponse = NettyResponseAdapter.wrap(httpObject)

			// TODO: prevent this from getting written to tape
			((HttpResponse) httpObject).headers().add(X_BETAMAX, "REC");
		}

		if (httpObject instanceof HttpContent) {
			upstreamResponse.append(httpObject)
		}

		if (httpObject instanceof LastHttpContent) {
			if (!tape.isWritable()) {
				throw new NonWritableTapeException();
			}

			log.log(INFO, "Recording to '" + tape.getName() + "'");
			tape.record(request, upstreamResponse);
		}
	}

	@Override
	void responsePost(HttpObject httpObject) {
		println "responsePost ${httpObject.getClass().name}"
		if (httpObject instanceof HttpResponse) {
			def response = (HttpResponse) httpObject
			response.headers().set(VIA, VIA_HEADER)
		}
	}
}
