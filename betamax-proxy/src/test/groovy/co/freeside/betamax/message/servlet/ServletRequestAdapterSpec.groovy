package co.freeside.betamax.message.servlet

import javax.servlet.http.HttpServletRequest
import co.freeside.betamax.util.servlet.MockServletInputStream
import org.apache.commons.collections.iterators.*
import spock.lang.Specification
import static org.apache.http.HttpHeaders.*

class ServletRequestAdapterSpec extends Specification {

	private static final IteratorEnumeration EMPTY_ENUMERATION = new IteratorEnumeration(EmptyIterator.INSTANCE)

	HttpServletRequest servletRequest = Mock(HttpServletRequest)

	void 'request can read basic fields'() {
		given:
		servletRequest.method >> 'GET'
		servletRequest.requestURL >> new StringBuffer('http://freeside.co/betamax')

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.method == 'GET'
		request.uri == new URI('http://freeside.co/betamax')
	}

	void 'request target includes query string'() {
		given:
		servletRequest.requestURL >> new StringBuffer('http://freeside.co/betamax')
		servletRequest.queryString >> 'q=1'

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.uri == new URI('http://freeside.co/betamax?q=1')
	}

	void 'request can read headers'() {
		given:
		servletRequest.headerNames >> new IteratorEnumeration([IF_NONE_MATCH, ACCEPT_ENCODING].iterator())
		servletRequest.getHeaders(IF_NONE_MATCH) >> new IteratorEnumeration(['abc123'].iterator())
		servletRequest.getHeaders(ACCEPT_ENCODING) >> new IteratorEnumeration(['gzip', 'deflate'].iterator())

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.getHeader(IF_NONE_MATCH) == 'abc123'
		request.getHeader(ACCEPT_ENCODING) == 'gzip, deflate'
	}

	void 'request headers are immutable'() {
		given:
		servletRequest.headerNames >> EMPTY_ENUMERATION
		servletRequest.getHeaders(_) >> EMPTY_ENUMERATION

		and:
		def request = new ServletRequestAdapter(servletRequest)

		when:
		request.headers[IF_NONE_MATCH] = ['abc123']

		then:
		thrown UnsupportedOperationException
	}

	void 'request body is readable as text'() {
		given:
		def bodyText = 'value=\u00a31'
		def bodyBytes = bodyText.getBytes('ISO-8859-1')
		servletRequest.inputStream >> new MockServletInputStream(new ByteArrayInputStream(bodyBytes))
		servletRequest.contentType >> 'application/x-www-form-urlencoded; charset=ISO-8859-1'
		servletRequest.contentLength >> 8
		servletRequest.characterEncoding >> 'ISO-8859-1'
		servletRequest.headerNames >> EMPTY_ENUMERATION
		servletRequest.getHeaders(_) >> EMPTY_ENUMERATION

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.hasBody()
		request.bodyAsText.text == bodyText
	}

	void 'request body is readable as binary'() {
		given:
		def body = 'value=\u00a31'.getBytes('ISO-8859-1')
		servletRequest.inputStream >> new MockServletInputStream(new ByteArrayInputStream(body))
		servletRequest.contentType >> 'application/x-www-form-urlencoded; charset=ISO-8859-1'
		servletRequest.contentLength >> 8
		servletRequest.characterEncoding >> 'ISO-8859-1'

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.hasBody()
		request.bodyAsBinary.bytes == body
	}

}
