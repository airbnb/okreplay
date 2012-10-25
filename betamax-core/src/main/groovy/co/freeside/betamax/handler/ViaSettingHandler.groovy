package co.freeside.betamax.handler

import co.freeside.betamax.message.*
import static co.freeside.betamax.Headers.VIA_HEADER
import static org.apache.http.HttpHeaders.VIA

class ViaSettingHandler extends ChainedHttpHandler {

	@Override
	Response handle(Request request) {
		def response = chain request
		response.addHeader VIA, VIA_HEADER
		response
	}

}
