package co.freeside.betamax.proxy

import io.netty.handler.codec.http.HttpRequest
import org.littleshoot.proxy.*

class BetamaxFiltersSource extends HttpFiltersSourceAdapter {

	@Override
	HttpFilters filterRequest(HttpRequest originalRequest) {
		return new BetamaxFilters(originalRequest);
	}
}
