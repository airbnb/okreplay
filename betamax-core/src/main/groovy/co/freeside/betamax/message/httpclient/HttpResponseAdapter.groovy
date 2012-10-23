package co.freeside.betamax.message.httpclient

import co.freeside.betamax.message.Response
import org.apache.http.HttpResponse

class HttpResponseAdapter extends HttpMessageAdapter<HttpResponse> implements Response {

	private final HttpResponse delegate
	private final byte[] body

	HttpResponseAdapter(HttpResponse delegate) {
		this.delegate = delegate

		if (delegate.entity) {
			body = delegate.entity.content.bytes
		}
	}

	@Override
	protected HttpResponse getDelegate() {
		delegate
	}

	@Override
	int getStatus() {
		delegate.statusLine.statusCode
	}

	@Override
	boolean hasBody() {
		body != null
	}

	@Override
	InputStream getBodyAsBinary() {
		if (body == null) {
			throw new IllegalStateException('cannot read the body of a response that does not have one')
		}
		new ByteArrayInputStream(body)
	}

	@Override
	Reader getBodyAsText() {
		new InputStreamReader(bodyAsBinary, charset)
	}

}
