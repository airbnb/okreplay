package co.freeside.betamax.message.http

import co.freeside.betamax.message.AbstractMessage
import co.freeside.betamax.message.Request
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpRequest

class HttpRequestAdapter extends AbstractMessage implements Request {

	private final HttpRequest delegate

	HttpRequestAdapter(HttpRequest delegate) {
		this.delegate = delegate
	}

	@Override
	String getMethod() {
		delegate.requestLine.method
	}

	@Override
	URI getUri() {
		delegate.requestLine.uri.toURI()
	}

	@Override
	Map<String, String> getHeaders() {
		delegate.allHeaders.inject([:]) { map, header ->
			map << new MapEntry(header.name, getHeader(header.name))
		}
	}

	@Override
	void addHeader(String name, String value) {
		delegate.addHeader(name, value)
	}

	@Override
	boolean hasBody() {
		delegate instanceof HttpEntityEnclosingRequest
	}

	@Override
	InputStream getBodyAsBinary() {
		if (delegate instanceof HttpEntityEnclosingRequest) {
			delegate.entity.content
		} else {
			throw new IllegalStateException('no body')
		}
	}
}
