package co.freeside.betamax.message.httpclient

import co.freeside.betamax.message.Request
import org.apache.http.*

class HttpRequestAdapter extends HttpMessageAdapter<HttpRequest> implements Request {

	private final HttpRequest delegate

	HttpRequestAdapter(HttpRequest delegate) {
		this.delegate = delegate
	}

	@Override
	protected HttpRequest getDelegate() {
		delegate
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
