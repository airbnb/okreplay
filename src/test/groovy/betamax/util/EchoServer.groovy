package betamax.util

import groovy.util.logging.Log4j
import java.util.concurrent.CountDownLatch
import static java.util.concurrent.TimeUnit.SECONDS

/**
 * A very simple socket server that listens for _one_ request and responds by just echoing back the request content. The
 * server shuts down after serving a single request or after a short timeout but can be restarted by calling `start()`
 * again.
 */
@Log4j
class EchoServer {

	/**
	 * The maximum time in milliseconds the server will wait for a request before giving up and shutting down.
	 */
	int timeout = SECONDS.toMillis(1)

	private final String host
	private final int port
	private Thread t
	private CountDownLatch readyLatch
	private CountDownLatch doneLatch

	EchoServer() {
		host = InetAddress.localHost.hostAddress
		port = 5000
	}

	String getUrl() {
		"http://$host:$port/"
	}

	void start() {
		readyLatch = new CountDownLatch(1)
		doneLatch = new CountDownLatch(1)

		t = Thread.start {
			def server = new ServerSocket(port)
			server.soTimeout = timeout
			try {
				readyLatch.countDown()
				server.accept { socket ->
					log.debug "got request..."
					socket.withStreams { input, output ->
						output.withWriter { writer ->
							writer << "HTTP/1.1 200 OK\n"
							writer << "Content-Type: text/plain\n\n"
							while (input.available()) {
								writer << (char) input.read()
							}
						}
					}
					log.debug "response sent..."
				}
			} catch (SocketTimeoutException e) {
				log.warn "no connection within $timeout  milliseconds, giving up..."
			} finally {
				log.debug "shutting down..."
				server.close()
				doneLatch.countDown()
			}
		}

		readyLatch.await()
	}

	void awaitStop() {
		doneLatch.await()
	}

}
