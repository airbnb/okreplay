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

package betamax.proxy

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.HttpConnectionParams
import org.apache.http.util.EntityUtils
import org.apache.log4j.Logger
import static java.net.HttpURLConnection.*
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import org.apache.http.entity.*
import org.apache.http.impl.client.*
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
		def requestWrapper = request instanceof HttpEntityEnclosingRequest ? new EntityBufferingRequestWrapper(request) : new RequestWrapper(request)

		boolean handled = interceptor?.interceptRequest(requestWrapper, response)
		if (!handled) {
			try {
				def proxyRequest = createProxyRequest(requestWrapper)
				def proxyResponse = httpClient.execute(proxyRequest)
				copyResponseData(proxyResponse, response)
				interceptor?.interceptResponse(requestWrapper, response)
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

	private HttpUriRequest createProxyRequest(HttpRequest request) {
		def proxyRequest = request instanceof HttpEntityEnclosingRequest ? new EntityEnclosingRequestWrapper(request) : new RequestWrapper(request)
		for (headerName in NO_PASS_HEADERS) {
			proxyRequest.removeHeaders(headerName)
		}
		proxyRequest.addHeader(VIA, "Betamax")

		HttpConnectionParams.setConnectionTimeout(request.params, timeout)
		HttpConnectionParams.setSoTimeout(request.params, timeout)

		proxyRequest
	}

	private void copyResponseData(HttpResponse from, HttpResponse to) {
		to.statusCode = from.statusLine.statusCode
		for (header in from.allHeaders) {
			if (!(header.name in NO_PASS_HEADERS)) {
				to.addHeader(header)
			}
		}
		if (from.entity) {
			to.entity = copyEntity(from.entity)
		}
	}

	private HttpEntity copyEntity(HttpEntity entity) {
		if (entity.isRepeatable()) {
			log.debug "re-using repeatable entity with content type ${entity.contentType?.value}..."
			new HttpEntityWrapper(entity)
		} else {
			log.debug "copying non-repeatable entity ${entity.getClass().name} with content type ${entity.contentType?.value}..."
			def copy = new ByteArrayEntity(EntityUtils.toByteArray(entity))
			copy.chunked = entity.chunked
			copy.contentEncoding = entity.contentEncoding
			copy.contentType = entity.contentType
			log.debug "copied entity..."
			copy
		}
	}

}
