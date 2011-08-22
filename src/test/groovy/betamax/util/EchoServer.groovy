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

import groovy.util.logging.Log4j
import java.util.concurrent.CountDownLatch
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener
import org.eclipse.jetty.util.component.LifeCycle
import static java.net.HttpURLConnection.HTTP_OK
import javax.servlet.http.*
import org.eclipse.jetty.server.*

@Log4j
class EchoServer extends AbstractLifeCycleListener {

	private final String host
	private final int port
	private Server server
	private CountDownLatch startedLatch
	private CountDownLatch stoppedLatch

	EchoServer() {
		host = InetAddress.localHost.hostAddress
		port = 5000
	}

	String getUrl() {
		"http://$host:$port/"
	}

	void start() {
		startedLatch = new CountDownLatch(1)
		stoppedLatch = new CountDownLatch(1)

		server = new Server(port)
		server.handler = new EchoHandler()
		server.addLifeCycleListener(this)
		server.start()

		startedLatch.await()
	}

	void stop() {
		if (server) {
			server.stop()
			stoppedLatch.await()
		}
	}

	@Override
	void lifeCycleStarted(LifeCycle event) {
        log.debug "started..."
		startedLatch.countDown()
	}

	@Override
	void lifeCycleStopped(LifeCycle event) {
        log.debug "stopped..."
		stoppedLatch.countDown()
	}

}

@Log4j
class EchoHandler extends AbstractHandler {

	void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		log.debug "received $request.method request for $target"
		response.status = HTTP_OK
		response.contentType = "text/plain"
		response.writer.withWriter { writer ->
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

}
