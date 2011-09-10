/*
 * Copyright 2011 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package betamax.proxy.httpcore

import org.apache.http.client.HttpClient
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.HttpConnectionParams
import org.apache.log4j.Logger
import betamax.proxy.*
import static java.net.HttpURLConnection.*
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import org.apache.http.client.methods.*
import org.apache.http.protocol.*

class HttpProxyHandler implements HttpRequestHandler {

	/**
	 * These headers are stripped from the proxied request and response.
	 */
	private static final NO_PASS_HEADERS = [
			CONTENT_LENGTH,
			HOST,
			PROXY_CONNECTION,
			CONNECTION,
			KEEP_ALIVE,
			PROXY_AUTHENTICATE,
			PROXY_AUTHORIZATION,
			TE,
			TRAILER,
			TRANSFER_ENCODING,
			UPGRADE
	].asImmutable()
	private static final String PROXY_CONNECTION = "Proxy-Connection"
	private static final String KEEP_ALIVE = "Keep-Alive"

	HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager())
	int timeout
	VetoingProxyInterceptor interceptor

	private final log = Logger.getLogger(HttpProxyHandler)

	void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		log.debug "proxying request $request.requestLine..."
		def requestWrapper = new HttpCoreRequestImpl(request)
		def responseWrapper = new HttpCoreResponseImpl(response)

		boolean handled = interceptor?.interceptRequest(requestWrapper, responseWrapper)
		if (!handled) {
			try {
				def proxyRequest = createProxyRequest(requestWrapper)
				def proxyResponse = httpClient.execute(proxyRequest)
				copyResponseData(proxyResponse, responseWrapper)
				interceptor?.interceptResponse(requestWrapper, responseWrapper)
			} catch (SocketTimeoutException e) {
				log.error "timed out connecting to $request.requestLine.uri"
				response.statusCode = HTTP_GATEWAY_TIMEOUT
				response.reasonPhrase = "Target server took too long to respond"
			} catch (IOException e) {
				log.error "problem connecting to $request.requestLine.uri", e
				response.statusCode = HTTP_BAD_GATEWAY
				response.reasonPhrase = e.message
			} catch (Exception e) {
				log.fatal "error recording HTTP exchange", e
				response.statusCode = HTTP_INTERNAL_ERROR
				response.reasonPhrase = e.message
			}
		}

		response.addHeader(VIA, "Betamax")
		log.debug "proxied request complete with response code ${response.statusLine.statusCode}..."
	}

	private HttpUriRequest createProxyRequest(Request request) {
		def proxyRequest = newRequestInstance(request)
		for (header in request.headers) {
			if (!(header.key in NO_PASS_HEADERS)) {
				for (value in header.value) {
					proxyRequest.addHeader(header.key, value)
				}
			}
		}
		proxyRequest.addHeader(VIA, "Betamax")
		if (request.hasBody()) {
			proxyRequest.entity = new ByteArrayEntity(request.bodyAsBinary.bytes)
		}

		HttpConnectionParams.setConnectionTimeout(proxyRequest.params, timeout)
		HttpConnectionParams.setSoTimeout(proxyRequest.params, timeout)

		proxyRequest
	}

	HttpUriRequest newRequestInstance(Request request) {
		switch (request.method) {
			case "DELETE": return new HttpDelete(request.target.toString())
			case "GET": return new HttpGet(request.target.toString())
			case "HEAD": return new HttpHead(request.target.toString())
			case "OPTIONS": return new HttpOptions(request.target.toString())
			case "POST": return new HttpPost(request.target.toString())
			case "PUT": return new HttpPut(request.target.toString())
			case "TRACE": return new HttpTrace(request.target.toString())
		}
	}

	private void copyResponseData(HttpResponse from, Response to) {
		to.status = from.statusLine.statusCode
		to.reason = from.statusLine.reasonPhrase
		for (header in from.allHeaders) {
			if (!(header.name in NO_PASS_HEADERS)) {
				to.addHeader(header.name, header.value)
			}
		}
		if (from.entity) {
			to.outputStream.withStream {
				it << from.entity.content
			}
		}
	}

}
