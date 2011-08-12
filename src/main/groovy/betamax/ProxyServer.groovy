package betamax

class ProxyServer implements Runnable {

	static final int portNumber = 5555

	private boolean running

	static void main(String... args) {
		def server = new ProxyServer()
		new Thread(server).start()
	}

	void run() {
		println "Starting the MyProxyServer ..."
		try {
			def serverSocket = new ServerSocket(portNumber, 1)

			running = true
			while (running) {
				println "Waiting for connection"
				serverSocket.accept() { socket ->
					println "Connection to ProxyServer is connected"
					socket.withStreams { input, output ->
						println "Client has asked to...\n$input.text"
						output.withWriter { writer ->
							writer << "HTTP/1.1 200 OK\n"
							writer << "Content-Type: text/html\n\n"
							writer << "<html><body>Hello World! It's ${new Date()}</body></html>\n"
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace()
		}
	}
}
