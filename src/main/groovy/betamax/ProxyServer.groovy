package betamax

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ProxyServer implements Runnable {

	static final int portNumber = 5555

	private AtomicBoolean running = new AtomicBoolean(false)
	private CountDownLatch latch = new CountDownLatch(1)

	void run() {
		println "Starting the ProxyServer ..."
		def listenSocket
		try {
			listenSocket = new ServerSocket(portNumber, 1)

			running.set(true)
			latch.countDown()
			while (running) {
				println "Waiting for connection"
				listenSocket.accept() { socket ->
					println "Connection to ProxyServer established"
					socket.withStreams { input, output ->
						println "Got request..."
						def command = new BufferedReader(new InputStreamReader(input)).readLine()
						println "Client has asked to...\n$command "
						output.withWriter { writer ->
							println "Sending dummy response..."
							writer << "HTTP/1.1 200 OK\n"
							writer << "Content-Type: text/plain\n\n"
							writer << "Hello from the proxy! It's ${new Date()}\n"
						}
					}
				}
			}
		} finally {
			println "Killing server"
			listenSocket?.close()
		}
	}

	void stop() {
		running.set(false)
	}

	boolean waitUntilRunning(long l, TimeUnit timeUnit) {
		latch.await(l, timeUnit)
	}
}
