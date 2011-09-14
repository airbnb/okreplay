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

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.HttpConnectionParams
import org.apache.log4j.Logger
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import static java.net.HttpURLConnection.*
import javax.servlet.http.*
import static org.apache.http.HttpHeaders.*
import org.apache.http.client.methods.*
import betamax.proxy.VetoingProxyInterceptor
import betamax.proxy.servlet.ServletRequestImpl
import betamax.proxy.servlet.ServletResponseImpl
import betamax.proxy.Response

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

    private final Logger log = Logger.getLogger(ProxyHandler)

	void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        log.debug "proxying request $request.method: $request.requestURI..."

		def requestWrapper = new ServletRequestImpl(request)
		def responseWrapper = new ServletResponseImpl(response)

		responseWrapper.addHeader(VIA, "Betamax")
        boolean handled = interceptor?.interceptRequest(requestWrapper, responseWrapper)
        if (!handled) {
            try {
				proceedRequest(requestWrapper, responseWrapper)
                interceptor?.interceptResponse(requestWrapper, responseWrapper)
            } catch (SocketTimeoutException e) {
                log.error "timed out connecting to $requestWrapper.target"
                response.sendError(HTTP_GATEWAY_TIMEOUT, "Target server took too long to respond")
            } catch (IOException e) {
                log.error "problem connecting to $requestWrapper.target", e
                response.sendError(HTTP_BAD_GATEWAY, e.message)
            } catch (Exception e) {
                log.fatal "error recording HTTP exchange", e
                response.sendError(HTTP_INTERNAL_ERROR, e.message)
            }
        }

        log.debug "proxied request complete with response code ${responseWrapper.status} and content type ${responseWrapper.contentType}..."
    }

	private void proceedRequest(betamax.proxy.Request request, Response response) {
		def proxyRequest = createProxyRequest(request)
		def proxyResponse = httpClient.execute(proxyRequest)
		copyResponseData(proxyResponse, response)
	}

	private HttpUriRequest createProxyRequest(betamax.proxy.Request request) {
        def proxyRequest
		switch(request.method) {
			case "DELETE":
				proxyRequest = new HttpDelete(request.target); break
			case "GET":
				proxyRequest = new HttpGet(request.target); break
			case "HEAD":
				proxyRequest = new HttpHead(request.target); break
			case "OPTIONS":
				proxyRequest = new HttpOptions(request.target); break
			case "POST":
				proxyRequest = new HttpPost(request.target)
				proxyRequest.entity = new ByteArrayEntity(request.bodyAsBinary.bytes)
				break
			case "PUT":
				proxyRequest = new HttpPut(request.target)
				proxyRequest.entity = new ByteArrayEntity(request.bodyAsBinary.bytes)
				break
			case "TRACE":
				proxyRequest = new HttpTrace(request.target); break
		}

		for (header in request.headers) {
            if (!(header.key in NO_PASS_HEADERS)) {
                for (value in header.value) {
                    proxyRequest.addHeader(header.key, value)
                }
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
            to.outputStream.withStream {
				it << from.entity.content
			}
        } else {
			to.outputStream.close()
		}
    }

}