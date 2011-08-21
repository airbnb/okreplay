package betamax.util

import groovy.util.logging.Log4j
import org.eclipse.jetty.server.handler.AbstractHandler
import static java.net.HttpURLConnection.HTTP_OK
import javax.servlet.http.*
import org.eclipse.jetty.server.*

@Log4j
class EchoServer {

	private final String host
	private final int port
	private Server server

	EchoServer() {
		host = InetAddress.localHost.hostAddress
		port = 5000
	}

	String getUrl() {
		"http://$host:$port/"
	}

	void start() {
		server = new Server(port)
		server.handler = new EchoHandler()
		server.start()
	}

	void stop() {
		server.stop()
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
