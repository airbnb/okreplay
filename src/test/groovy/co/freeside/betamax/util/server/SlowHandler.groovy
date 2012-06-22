package co.freeside.betamax.util.server

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

import java.util.concurrent.CountDownLatch
import java.util.logging.Logger
import javax.servlet.http.*

import static java.util.concurrent.TimeUnit.SECONDS

/**
 * A very dumb handler that will simply sit on any requests until it is told to shut down (i.e. the server is shutting
 * down). This is used for testing timeout conditions on clients.
 */
class SlowHandler extends AbstractHandler {

	private final log = Logger.getLogger(SlowHandler.name)

	private final CountDownLatch stopLatch = new CountDownLatch(1)

	void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		log.fine "received $request.method request for $target..."
		stopLatch.await(30, SECONDS)
		log.fine "request complete..."
	}

	@Override
	protected void doStop() {
		log.fine "stopping handler..."
		stopLatch.countDown()
		super.doStop()
	}

}
