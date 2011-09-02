package betamax.util

import org.apache.log4j.Logger
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.*
import java.util.concurrent.CountDownLatch

/**
 * A very dumb handler that will simply sit on any requests until it is told to shut down (i.e. the server is shutting
 * down). This is used for testing timeout conditions on clients.
 */
class SlowHandler extends AbstractHandler {

	private final log = Logger.getLogger(SlowHandler)

	private final CountDownLatch stopLatch = new CountDownLatch(1)

	void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		log.debug "received $request.method request for $target..."
		stopLatch.await()
		log.debug "dying..."
	}

	@Override
	protected void doStop() {
		log.debug "Oh, am I being a bit slow?"
		stopLatch.countDown()
		super.doStop()
	}

}
