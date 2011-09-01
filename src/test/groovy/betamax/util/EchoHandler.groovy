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

package betamax.util

import org.apache.log4j.Logger
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import java.util.zip.*
import javax.servlet.http.*

class EchoHandler extends AbstractHandler {

	private final log = Logger.getLogger(EchoHandler)

	void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		log.debug "received $request.method request for $target"
		response.status = java.net.HttpURLConnection.HTTP_OK
		response.contentType = "text/plain"

		getResponseWriter(request, response).withWriter { writer ->
			writer << request.method << " " << request.requestURI
			if (request.queryString) {
				writer << "?" << request.queryString
			}
			writer << " " << request.protocol << "\n"
			for (headerName in request.headerNames) {
				for (header in request.getHeaders(headerName)) {
					writer << headerName << ": " << header << "\n"
				}
			}
			request.reader.withReader { reader ->
				while (reader.ready()) {
					writer << (char) reader.read()
				}
			}
		}
	}

	private Writer getResponseWriter(HttpServletRequest request, HttpServletResponse response) {
		def out
		def acceptedEncodings = request.getHeader(org.eclipse.jetty.http.HttpHeaders.ACCEPT_ENCODING)?.tokenize(",")
		log.debug "request accepts $acceptedEncodings"
		if ("gzip" in acceptedEncodings) {
			response.addHeader(org.eclipse.jetty.http.HttpHeaders.CONTENT_ENCODING, "gzip")
			out = new OutputStreamWriter(new GZIPOutputStream(response.outputStream))
		} else if ("deflate" in acceptedEncodings) {
			response.addHeader(org.eclipse.jetty.http.HttpHeaders.CONTENT_ENCODING, "deflate")
			out = new OutputStreamWriter(new DeflaterOutputStream(response.outputStream))
		} else {
			response.addHeader(org.eclipse.jetty.http.HttpHeaders.CONTENT_ENCODING, "none")
			out = response.writer
		}
		out
	}

}
