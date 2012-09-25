package co.freeside.betamax.message.http

import co.freeside.betamax.message.AbstractMessage
import co.freeside.betamax.message.Response
import org.apache.http.HttpResponse

class HttpResponseAdapter extends AbstractMessage implements Response {

	private final HttpResponse delegate
	private final byte[] body

	HttpResponseAdapter(HttpResponse delegate) {
		this.delegate = delegate

		if (delegate.entity) {
			body = delegate.entity.content.bytes
		}
	}

	int getStatus() {
		delegate.statusLine.statusCode
	}

	Map<String, String> getHeaders() {
        	delegate.allHeaders.inject([:]) { map, header ->
            		map << new MapEntry(header.name, getHeader(header.name))
        	}
	}

	@Override
	String getHeader(String name) {
		delegate.getHeaders(name).value.join(', ')
	}

	void addHeader(String name, String value) {
		delegate.addHeader(name, value)
	}

	boolean hasBody() {
		body != null
	}

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
