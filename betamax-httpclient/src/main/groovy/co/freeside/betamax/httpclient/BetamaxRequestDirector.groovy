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

package co.freeside.betamax.httpclient

import co.freeside.betamax.Recorder
import co.freeside.betamax.handler.*
import co.freeside.betamax.message.httpclient.HttpRequestAdapter
import co.freeside.betamax.util.Network
import org.apache.http.*
import org.apache.http.client.RequestDirector
import org.apache.http.entity.*
import org.apache.http.impl.EnglishReasonPhraseCatalog
import org.apache.http.message.BasicHttpResponse
import org.apache.http.protocol.HttpContext
import static java.util.Locale.ENGLISH
import static org.apache.http.HttpVersion.HTTP_1_1

class BetamaxRequestDirector implements RequestDirector {

	private final RequestDirector delegate
	private final Recorder recorder
	private final HttpHandler handlerChain

	BetamaxRequestDirector(RequestDirector delegate, Recorder recorder) {
		this.delegate = delegate
		this.recorder = recorder

		handlerChain = new DefaultHandlerChain(recorder)
	}

	@Override
	HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) {
		if (shouldIgnore(target)) {
			delegate.execute target, request, context
		} else {
			handleRequest request
		}
	}

	private HttpResponse handleRequest(HttpRequest request) {
		def requestWrapper = new HttpRequestAdapter(request)
		def responseWrapper = handlerChain.handle(requestWrapper)

		def response = new BasicHttpResponse(
				HTTP_1_1,
				responseWrapper.status,
				EnglishReasonPhraseCatalog.INSTANCE.getReason(responseWrapper.status, ENGLISH)
		)
		responseWrapper.headers.each { name, value ->
			value.tokenize(',').each {
				response.addHeader(name, it.trim())
			}
		}
		if (responseWrapper.hasBody()) {
			response.entity = new ByteArrayEntity(responseWrapper.bodyAsBinary.input.bytes, ContentType.create(responseWrapper.contentType))
		}
		response
	}

	private boolean shouldIgnore(HttpHost target) {
		def ignoredHosts = recorder.ignoreHosts
		if (recorder.ignoreLocalhost) {
			ignoredHosts.addAll(Network.localAddresses)
		}
		target.hostName in ignoredHosts
	}
}
