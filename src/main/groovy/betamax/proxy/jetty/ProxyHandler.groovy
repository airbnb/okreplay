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

package betamax.proxy.jetty

import java.util.logging.Logger
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.HttpConnectionParams
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import betamax.proxy.*
import betamax.proxy.servlet.*
import static java.net.HttpURLConnection.*
import static java.util.logging.Level.SEVERE
import javax.servlet.http.*
import static org.apache.http.HttpHeaders.*
import org.apache.http.client.methods.*

class ProxyHandler extends AbstractHandler {

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

	private final Logger log = Logger.getLogger(ProxyHandler.name)

	void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		log.fine "proxying request $request.method: $request.requestURI..."

		def requestWrapper = new ServletRequestImpl(request)
		def responseWrapper = new ServletResponseImpl(response)

		responseWrapper.addHeader(VIA, "Betamax")
		boolean handled = interceptor?.interceptRequest(requestWrapper, responseWrapper)
		if (!handled) {
			try {
				proceedRequest(requestWrapper, responseWrapper)
				interceptor?.interceptResponse(requestWrapper, responseWrapper)
				responseWrapper.outputStream.close()
			} catch (SocketTimeoutException e) {
				log.severe "timed out connecting to $requestWrapper.uri"
				response.sendError(HTTP_GATEWAY_TIMEOUT, "Target server took too long to respond")
			} catch (IOException e) {
				log.log SEVERE, "problem connecting to $requestWrapper.uri", e
				response.sendError(HTTP_BAD_GATEWAY, e.message)
			} catch (Exception e) {
				log.log SEVERE, "error recording HTTP exchange", e
				response.sendError(HTTP_INTERNAL_ERROR, e.message)
			}
		}

		log.fine "proxied request complete with response code ${responseWrapper.status} and content type ${responseWrapper.contentType}..."
	}

	private void proceedRequest(betamax.proxy.Request request, Response response) {
		def proxyRequest = createProxyRequest(request)
		def proxyResponse = httpClient.execute(proxyRequest)
		copyResponseData(proxyResponse, response)
	}

	private HttpUriRequest createProxyRequest(betamax.proxy.Request request) {
		def proxyRequest
		switch (request.method) {
			case "DELETE":
				proxyRequest = new HttpDelete(request.uri); break
			case "GET":
				proxyRequest = new HttpGet(request.uri); break
			case "HEAD":
				proxyRequest = new HttpHead(request.uri); break
			case "OPTIONS":
				proxyRequest = new HttpOptions(request.uri); break
			case "POST":
				proxyRequest = new HttpPost(request.uri)
				proxyRequest.entity = new ByteArrayEntity(request.bodyAsBinary.bytes)
				break
			case "PUT":
				proxyRequest = new HttpPut(request.uri)
				proxyRequest.entity = new ByteArrayEntity(request.bodyAsBinary.bytes)
				break
			case "TRACE":
				proxyRequest = new HttpTrace(request.uri); break
		}

		for (header in request.headers) {
			if (!(header.key in NO_PASS_HEADERS)) {
				proxyRequest.addHeader(header.key, header.value)
			}
		}
		proxyRequest.addHeader(VIA, "Betamax")

		HttpConnectionParams.setConnectionTimeout(proxyRequest.params, timeout)
		HttpConnectionParams.setSoTimeout(proxyRequest.params, timeout)

		proxyRequest
	}

	private void copyResponseData(HttpResponse from, Response to) {
		to.status = from.statusLine.statusCode
		for (header in from.allHeaders) {
			if (!(header.name in NO_PASS_HEADERS)) {
				to.addHeader(header.name, header.value)
			}
		}
		if (from.entity) {
			to.outputStream << from.entity.content
		}
	}

}