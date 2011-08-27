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

package betamax.server

import betamax.Recorder
import groovy.util.logging.Log4j
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.util.EntityUtils
import static java.net.HttpURLConnection.*
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import org.apache.http.entity.*
import org.apache.http.impl.client.*
import org.apache.http.protocol.*

@Log4j
class HttpProxyHandler implements HttpRequestHandler {

	static final String X_BETAMAX = "X-Betamax"
	static final String PROXY_CONNECTION = "Proxy-Connection"
	static final String KEEP_ALIVE = "Keep-Alive"
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
	].toSet().asImmutable()

	private final HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager())
	private final Recorder recorder

	HttpProxyHandler(Recorder recorder) {
		this.recorder = recorder
	}

	void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		log.debug "proxying request $request.requestLine..."
		def requestWrapper = request instanceof HttpEntityEnclosingRequest ? new EntityBufferingRequestWrapper(request) : new RequestWrapper(request)

		def tape = recorder.tape

		if (!tape) {
			log.error "no tape inserted..."
			response.statusCode = HTTP_FORBIDDEN
			response.reasonPhrase = "No tape"
		} else if (tape.seek(requestWrapper) && tape.isReadable()) {
			log.info "playing back from tape '$tape.name'..."
			tape.play(response)
			response.addHeader(X_BETAMAX, "PLAY")
		} else {
			try {
				if (tape.isWritable()) {
					execute(requestWrapper, response)
					log.info "recording response with status $response.statusLine to tape '$tape.name'..."
					tape.record(requestWrapper, response)
					log.info "recording complete..."
					response.addHeader(X_BETAMAX, "REC")
				} else {
					response.statusCode = HTTP_FORBIDDEN
					response.reasonPhrase = "Tape is read-only"
				}
			} catch (IOException e) {
				// TODO: handle timeout by setting HTTP_GATEWAY_TIMEOUT
				log.error "problem connecting to $request.requestLine.uri", e
				response.statusCode = HTTP_BAD_GATEWAY
			} catch (Exception e) {
				log.fatal "error recording HTTP exchange", e
				response.statusCode = HTTP_INTERNAL_ERROR
				response.reasonPhrase = e.message
			}
		}

		response.addHeader(VIA, "Betamax")
		log.debug "proxied request complete..."
	}

	private void execute(HttpRequest request, HttpResponse response) {
		def proxyRequest = createProxyRequest(request)

		def proxyResponse = httpClient.execute(proxyRequest)

		copyResponseData(proxyResponse, response)
	}

	private HttpUriRequest createProxyRequest(HttpRequest request) {
		def proxyRequest = request instanceof HttpEntityEnclosingRequest ? new EntityEnclosingRequestWrapper(request) : new RequestWrapper(request)
		for (headerName in NO_PASS_HEADERS) {
			proxyRequest.removeHeaders(headerName)
		}
		proxyRequest.addHeader(VIA, "Betamax")
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
