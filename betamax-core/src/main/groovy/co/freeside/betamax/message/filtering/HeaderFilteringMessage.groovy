package co.freeside.betamax.message.filtering

import co.freeside.betamax.message.Message
import static org.apache.http.HttpHeaders.*

abstract class HeaderFilteringMessage implements Message {

	public static final NO_PASS_HEADERS = [
			CONTENT_LENGTH,
			HOST,
			PROXY_CONNECTION,
			CONNECTION,
			KEEP_ALIVE,
			PROXY_AUTHENTICATE,
			PROXY_AUTHORIZATION,
			TE,
			TRAILER,
			TRANSFER_ENCODING,
			UPGRADE
	].asImmutable()
	public static final String PROXY_CONNECTION = 'Proxy-Connection'
	public static final String KEEP_ALIVE = 'Keep-Alive'

	protected abstract Message getDelegate()

	final Map<String, String> getHeaders() {
		def headers = new HashMap(delegate.headers)
		for (headerName in NO_PASS_HEADERS) {
			headers.remove(headerName)
		}
		headers.asImmutable()
	}

	final String getHeader(String name) {
		name in NO_PASS_HEADERS ? null : delegate.getHeader(name)
	}

}
