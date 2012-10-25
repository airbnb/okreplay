package co.freeside.betamax.handler

import co.freeside.betamax.Recorder
import co.freeside.betamax.message.*
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient

/**
 * The default handler chain used by all Betamax implementations.
 */
class DefaultHandlerChain extends ChainedHttpHandler {

	DefaultHandlerChain(Recorder recorder, HttpClient httpClient) {
		this << new TapeReader(recorder) <<
				new TapeWriter(recorder) <<
				new HeaderFilter() <<
				new TargetConnector(httpClient)
	}

	DefaultHandlerChain(Recorder recorder) {
		this(recorder, new DefaultHttpClient())
	}

	@Override
	Response handle(Request request) {
		chain request
	}

}
