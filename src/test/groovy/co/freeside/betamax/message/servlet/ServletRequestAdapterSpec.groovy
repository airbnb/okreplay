package co.freeside.betamax.message.servlet

import co.freeside.betamax.util.servlet.MockServletInputStream
import org.apache.commons.collections.iterators.EmptyIterator
import org.apache.commons.collections.iterators.IteratorEnumeration
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class ServletRequestAdapterSpec extends Specification {

	private static final IteratorEnumeration EMPTY_ENUMERATION = new IteratorEnumeration(EmptyIterator.INSTANCE)

	HttpServletRequest servletRequest = Mock(HttpServletRequest)

	void "request can read basic fields"() {
		given:
		servletRequest.getMethod() >> "GET"
		servletRequest.getRequestURL() >> new StringBuffer("http://freeside.co/betamax")

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.method == "GET"
		request.uri == new URI("http://freeside.co/betamax")
	}

	void "request target includes query string"() {
		given:
		servletRequest.getRequestURL() >> new StringBuffer("http://freeside.co/betamax")
		servletRequest.getQueryString() >> "q=1"

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.uri == new URI("http://freeside.co/betamax?q=1")
	}

	void "request can read headers"() {
		given:
		servletRequest.getHeaderNames() >> new IteratorEnumeration(["If-None-Match", "Accept-Encoding"].iterator())
		servletRequest.getHeaders("If-None-Match") >> new IteratorEnumeration(["abc123"].iterator())
		servletRequest.getHeaders("Accept-Encoding") >> new IteratorEnumeration(["gzip", "deflate"].iterator())

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.getHeader("If-None-Match") == "abc123"
		request.getHeader("Accept-Encoding") == "gzip, deflate"
	}

	void "request headers are immutable"() {
		given:
		servletRequest.getHeaderNames() >> EMPTY_ENUMERATION
		servletRequest.getHeaders(_) >> EMPTY_ENUMERATION

		and:
		def request = new ServletRequestAdapter(servletRequest)

		when:
		request.headers["If-None-Match"] = ["abc123"]

		then:
		thrown UnsupportedOperationException
	}

	void "request body is readable as text"() {
		given:
		servletRequest.getInputStream() >> new MockServletInputStream(new ByteArrayInputStream("value=\u00a31".getBytes("ISO-8859-1")))
		servletRequest.getContentType() >> "application/x-www-form-urlencoded; charset=ISO-8859-1"
		servletRequest.getContentLength() >> 8
		servletRequest.getCharacterEncoding() >> "ISO-8859-1"
		servletRequest.getHeaderNames() >> EMPTY_ENUMERATION
		servletRequest.getHeaders(_) >> EMPTY_ENUMERATION

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.hasBody()
		request.bodyAsText.text == "value=\u00a31"
	}

	void "request body is readable as binary"() {
		given:
		servletRequest.getInputStream() >> new MockServletInputStream(new ByteArrayInputStream("value=\u00a31".getBytes("ISO-8859-1")))
		servletRequest.getContentType() >> "application/x-www-form-urlencoded; charset=ISO-8859-1"
		servletRequest.getContentLength() >> 8
		servletRequest.getCharacterEncoding() >> "ISO-8859-1"

		and:
		def request = new ServletRequestAdapter(servletRequest)

		expect:
		request.hasBody()
		request.bodyAsBinary.bytes == "value=\u00a31".getBytes("ISO-8859-1")
	}

}
