package co.freeside.betamax.util.server

import java.util.concurrent.atomic.AtomicInteger
import javax.servlet.http.*
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import static org.eclipse.jetty.http.HttpStatus.OK_200
import static org.eclipse.jetty.http.MimeTypes.TEXT_PLAIN

class IncrementingHandler extends AbstractHandler {

	private final AtomicInteger counter = new AtomicInteger()

	@Override
	void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		response.status = OK_200
		response.contentType = TEXT_PLAIN
		response.outputStream.withWriter { writer ->
			writer << 'count: ' << counter.incrementAndGet()
		}
	}

}
