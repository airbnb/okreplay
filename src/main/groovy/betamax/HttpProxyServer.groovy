/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package betamax

import org.apache.http.impl.nio.DefaultServerIOEventDispatch
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor
import org.apache.http.nio.NHttpConnection
import org.apache.http.params.SyncBasicHttpParams
import org.apache.http.util.EntityUtils
import org.apache.http.*
import static org.apache.http.HttpStatus.*
import org.apache.http.impl.*
import org.apache.http.nio.entity.*
import org.apache.http.nio.protocol.*
import static org.apache.http.params.CoreConnectionPNames.*
import static org.apache.http.params.CoreProtocolPNames.ORIGIN_SERVER
import org.apache.http.protocol.*

/**
 * Basic, yet fully functional and spec compliant, HTTP/1.1 server based on the non-blocking 
 * I/O model.
 * <p>
 * Please note the purpose of this application is demonstrate the usage of HttpCore APIs.
 * It is NOT intended to demonstrate the most efficient way of building an HTTP server. 
 */
class HttpProxyServer {

	static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Please specify document root directory")
			System.exit(1)
		}
		def params = new SyncBasicHttpParams()
		params.setIntParameter(SO_TIMEOUT, 5000).setIntParameter(SOCKET_BUFFER_SIZE, 8 * 1024).setBooleanParameter(STALE_CONNECTION_CHECK, false).setBooleanParameter(TCP_NODELAY, true).setParameter(ORIGIN_SERVER, "HttpComponents/1.1")

		def httpproc = new ImmutableHttpProcessor([
				new ResponseDate(),
				new ResponseServer(),
				new ResponseContent(),
				new ResponseConnControl()
		] as HttpResponseInterceptor[])

		def handler = new BufferingHttpServiceHandler(
				httpproc,
				new DefaultHttpResponseFactory(),
				new DefaultConnectionReuseStrategy(),
				params)

		// Set up request handlers
		def reqistry = new HttpRequestHandlerRegistry()
		reqistry.register "*", new HttpFileHandler(args[0])

		handler.handlerResolver = reqistry

		// Provide an event logger
		handler.eventListener = new EventLogger()

		def ioEventDispatch = new DefaultServerIOEventDispatch(handler, params)
		def ioReactor = new DefaultListeningIOReactor(2, params)
		try {
			ioReactor.listen(new InetSocketAddress(8080))
			ioReactor.execute(ioEventDispatch)
		} catch (InterruptedIOException ex) {
			System.err.println("Interrupted")
		} catch (IOException e) {
			System.err.println("I/O error: $e.message")
		}
		println "Shutdown"
	}

}

class HttpFileHandler implements HttpRequestHandler {

	private final String docRoot

	HttpFileHandler(final String docRoot) {
		super()
		this.docRoot = docRoot
	}

	void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) {

		def method = request.requestLine.method.toUpperCase(Locale.ENGLISH)
		if (!(method in ["GET", "HEAD", "POST"])) {
			throw new MethodNotSupportedException("$method method not supported")
		}

		if (request instanceof HttpEntityEnclosingRequest) {
			def entityContent = EntityUtils.toByteArray(request.entity)
			println "Incoming entity content (bytes): $entityContent.length"
		}

		def target = request.requestLine.uri
		final file = new File(docRoot, URLDecoder.decode(target, "UTF-8"))
		if (!file.exists()) {
			response.statusCode = SC_NOT_FOUND
			def entity = new NStringEntity("<html><body><h1>File$file.path not found</h1></body></html>", "UTF-8")
			entity.setContentType("text/html; charset=UTF-8")
			response.entity = entity
			println "File $file.path not found"
		} else if (!file.canRead() || file.isDirectory()) {
			response.statusCode = SC_FORBIDDEN
			def entity = new NStringEntity("<html><body><h1>Access denied</h1></body></html>", "UTF-8")
			entity.setContentType("text/html; charset=UTF-8")
			response.entity = entity
			println "Cannot read file $file.path"
		} else {
			response.statusCode = SC_OK
			def body = new NFileEntity(file, "text/html")
			response.entity = body
			println "Serving file $file.path"
		}
	}
}

class EventLogger implements EventListener {

	void connectionOpen(final NHttpConnection conn) {
		println "Connection open: $conn"
	}

	void connectionTimeout(final NHttpConnection conn) {
		println "Connection timed out: $conn"
	}

	void connectionClosed(final NHttpConnection conn) {
		println "Connection closed: $conn"
	}

	void fatalIOException(final IOException ex, final NHttpConnection conn) {
		System.err.println("I/O error: $ex.message")
	}

	void fatalProtocolException(final HttpException ex, final NHttpConnection conn) {
		System.err.println("HTTP error: $ex.message")
	}

}