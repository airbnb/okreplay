package co.freeside.betamax.proxy.jetty

import co.freeside.betamax.message.Response
import co.freeside.betamax.message.servlet.ServletRequestAdapter
import co.freeside.betamax.proxy.handler.HttpHandler
import co.freeside.betamax.proxy.handler.ProxyException
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.logging.Logger

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import static java.util.logging.Level.SEVERE
import static org.apache.http.HttpHeaders.VIA

class BetamaxProxy extends AbstractHandler {

	public static final String VIA_HEADER = 'Betamax'

	private HttpHandler handlerChain

	private static final Logger log = Logger.getLogger(BetamaxProxy.name)

	void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		def betamaxRequest = new ServletRequestAdapter(request)

		try {

			def betamaxResponse = handlerChain.handle(betamaxRequest)
			sendResponse(betamaxResponse, response)

		} catch (ProxyException e) {

			log.log SEVERE, 'exception in proxy processing', e
			response.sendError(e.httpStatus, e.message)

		} catch (Exception e) {

			log.log SEVERE, 'error recording HTTP exchange', e
			response.sendError(HTTP_INTERNAL_ERROR, e.message)

		}
	}

	HttpHandler leftShift(HttpHandler httpHandler) {
		handlerChain = httpHandler
		handlerChain
	}

	private void sendResponse(Response betamaxResponse, HttpServletResponse response) {
		response.status = betamaxResponse.status
		betamaxResponse.headers.each { name, value ->
			value.split(/,\s*/).each {
				response.addHeader(name, it)
			}
		}
		response.addHeader(VIA, VIA_HEADER)
		if (betamaxResponse.hasBody()) {
			response.outputStream.withStream { stream ->
				stream << betamaxResponse.bodyAsBinary
			}
		}
		response.flushBuffer()
	}

}
