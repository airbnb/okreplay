package co.freeside.betamax.message.tape

import co.freeside.betamax.encoding.*
import co.freeside.betamax.message.*
import org.apache.http.HttpHeaders

abstract class RecordedMessage extends AbstractMessage implements Message {

	Map<String, String> headers = [:]
	def body

	final void addHeader(String name, String value) {
		if (headers[name]) {
			headers[name] = "${headers[name]}, $value"
		} else {
			headers[name] = value
		}
	}

	final boolean hasBody() {
		body
	}

	@Override
	final Reader getBodyAsText() {
		String string
		if (body) {
			string = body instanceof String ? body : getEncoder().decode(bodyAsBinary, charset)
		} else {
			string = ''
		}

		new StringReader(string)
	}

	final InputStream getBodyAsBinary() {
		byte [] bytes
		if (body) {
			bytes = body instanceof String ? getEncoder().encode(body, charset) : body
		} else {
			bytes = new byte [0]
		}

		new ByteArrayInputStream(bytes)
	}

	private AbstractEncoder getEncoder() {
		switch (getHeader(HttpHeaders.CONTENT_ENCODING)) {
			case 'gzip':
				return new GzipEncoder()
			case 'deflate':
				return new DeflateEncoder()
			default:
				return new NoOpEncoder()
		}
	}
}
