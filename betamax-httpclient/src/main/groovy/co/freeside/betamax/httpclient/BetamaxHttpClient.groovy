package co.freeside.betamax.httpclient

import co.freeside.betamax.Recorder
import org.apache.http.ConnectionReuseStrategy
import org.apache.http.client.*
import org.apache.http.conn.*
import org.apache.http.conn.routing.HttpRoutePlanner
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.HttpParams
import org.apache.http.protocol.*

class BetamaxHttpClient extends DefaultHttpClient {

	private final Recorder recorder

	BetamaxHttpClient(Recorder recorder) {
		this.recorder = recorder
	}

	@Override
	protected RequestDirector createClientRequestDirector(
			HttpRequestExecutor requestExec,
			ClientConnectionManager conman,
			ConnectionReuseStrategy reustrat,
			ConnectionKeepAliveStrategy kastrat,
			HttpRoutePlanner rouplan,
			HttpProcessor httpProcessor,
			HttpRequestRetryHandler retryHandler,
			RedirectStrategy redirectStrategy,
			AuthenticationStrategy targetAuthStrategy,
			AuthenticationStrategy proxyAuthStrategy,
			UserTokenHandler userTokenHandler,
			HttpParams params) {
		def director = super.createClientRequestDirector(requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler, redirectStrategy, targetAuthStrategy, proxyAuthStrategy, userTokenHandler, params)
		new BetamaxRequestDirector(director, recorder)
	}

}

