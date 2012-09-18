package co.freeside.betamax.proxy.handler

import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response

interface HttpHandler {

	Response handle(Request request)

}