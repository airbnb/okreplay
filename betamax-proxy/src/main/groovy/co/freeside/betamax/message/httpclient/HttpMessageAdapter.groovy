package co.freeside.betamax.message.httpclient

import co.freeside.betamax.message.*
import org.apache.http.HttpMessage

abstract class HttpMessageAdapter<T extends HttpMessage> extends AbstractMessage implements Message {

	protected abstract T getDelegate()

	@Override
	final Map<String, String> getHeaders() {
		delegate.allHeaders.inject([:]) { map, header ->
			map << new MapEntry(header.name, getHeader(header.name))
		}
	}

	@Override
	final String getHeader(String name) {
		delegate.getHeaders(name).value.join(', ')
	}

	@Override
	final void addHeader(String name, String value) {
		delegate.addHeader(name, value)
	}

}
