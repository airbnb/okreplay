package co.freeside.betamax.handler

import co.freeside.betamax.message.*

interface HttpHandler {

	Response handle(Request request)

}