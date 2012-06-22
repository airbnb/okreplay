package co.freeside.betamax.proxy.servlet

import co.freeside.betamax.util.servlet.MockServletInputStream
import org.apache.commons.collections.iterators.IteratorEnumeration

import javax.servlet.ServletOutputStream

import co.freeside.betamax.encoding.*
import spock.lang.*

import javax.servlet.http.*

class ServletMessageImplSpec extends Specification {

	HttpServletRequest servletRequest = Mock(HttpServletRequest)
	HttpServletResponse servletResponse = Mock(HttpServletResponse)

	void "request can read basic fields"() {
		given:
		servletRequest.getMethod() >> "GET"
		servletRequest.getRequestURL() >> new StringBuffer("http://freeside.co/betamax")

		and:
		def request = new ServletRequestImpl(servletRequest)

		expect:
		request.method == "GET"
		request.uri == new URI("http://freeside.co/betamax")
	}

	void "request target includes query string"() {
		given:
		servletRequest.getRequestURL() >> new StringBuffer("http://freeside.co/betamax")
		servletRequest.getQueryString() >> "q=1"

		and:
		def request = new ServletRequestImpl(servletRequest)

		expect:
		request.uri == new URI("http://freeside.co/betamax?q=1")
	}

	void "request can read headers"() {
		given:
		servletRequest.getHeaderNames() >> new IteratorEnumeration(["If-None-Match", "Accept-Encoding"].iterator())
		servletRequest.getHeaders("If-None-Match") >> new IteratorEnumeration(["abc123"].iterator())
		servletRequest.getHeaders("Accept-Encoding") >> new IteratorEnumeration(["gzip", "deflate"].iterator())

		and:
		def request = new ServletRequestImpl(servletRequest)

		expect:
		request.getHeader("If-None-Match") == "abc123"
		request.getHeader("Accept-Encoding") == "gzip, deflate"
	}

	void "request headers are immutable"() {
		given:
		def request = new ServletRequestImpl(servletRequest)

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

		and:
		def request = new ServletRequestImpl(servletRequest)

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
		def request = new ServletRequestImpl(servletRequest)

		expect:
		request.hasBody()
		request.bodyAsBinary.bytes == "value=\u00a31".getBytes("ISO-8859-1")
	}

	void "response can read basic fields"() {
		given:
		def response = new ServletResponseImpl(servletResponse)

		when: "status and reason are set"
		response.status = 200

		then: "they can be retrieved again"
		response.status == 200

		and: "the underlying servlet response values are set"
		1 * servletResponse.setStatus(200)
	}

	void "response can add and read headers"() {
		given:
		def response = new ServletResponseImpl(servletResponse)

		when: "headers are added"
		response.addHeader("E-Tag", "abc123")
		response.addHeader("Vary", "Content-Language")
		response.addHeader("Vary", "Content-Type")

		then: "they can be retrieved again"
		response.getHeader("E-Tag") == "abc123"
		response.getHeader("Vary") == "Content-Language, Content-Type"

		and: "they are added to the underlying servlet response"
		1 * servletResponse.addHeader("E-Tag", "abc123")
		1 * servletResponse.addHeader("Vary", "Content-Language")
		1 * servletResponse.addHeader("Vary", "Content-Type")
	}

	void "content type header is handled by delegating to setContentType"() {
		def response = new ServletResponseImpl(servletResponse)

		when:
		response.addHeader("Content-Type", "text/html; charset=ISO-8859-1")

		then:
		1 * servletResponse.setContentType("text/html; charset=ISO-8859-1")
		0 * servletResponse.addHeader(_, _)

		and:
		response.getHeader("Content-Type") == "text/html; charset=ISO-8859-1"
	}

	void "response headers are immutable"() {
		given:
		def response = new ServletResponseImpl(servletResponse)

		when:
		response.headers["E-Tag"] = ["abc123"]

		then:
		thrown UnsupportedOperationException
	}

	void "response reports having no body before it is written to"() {
		given:
		def response = new ServletResponseImpl(servletResponse)

		expect:
		!response.hasBody()
	}

	@Unroll("response body can be written to and read from as #charset text")
	void "response body can be written to and read from as text"() {
		given: "the underlying servlet response writer"
		def servletOutputStream = new ByteArrayOutputStream()
		servletResponse.getOutputStream() >> new ServletOutputStream() {
			@Override
			void write(int b) {
				servletOutputStream.write(b)
			}
		}

		and: "the underlying response charset has been set"
		servletResponse.getCharacterEncoding() >> charset

		and:
		def response = new ServletResponseImpl(servletResponse)

		when: "the response is written to"
		response.writer.withWriter {
			it << "O HAI! \u00a31 KTHXBYE"
		}

		then: "the content can be read back as text"
		response.hasBody()
		response.bodyAsText.text == "O HAI! \u00a31 KTHXBYE"

		and: "the underlying servlet response is written to"
		servletOutputStream.toByteArray() == "O HAI! \u00a31 KTHXBYE".getBytes(charset)

		where:
		charset << ["ISO-8859-1", "UTF-8"]
	}

	void "response body can be written to and read from as binary"() {
		given: "the underlying servlet response output stream"
		def servletOutputStream = new ByteArrayOutputStream()
		servletResponse.getOutputStream() >> new ServletOutputStream() {
			@Override
			void write(int b) {
				servletOutputStream.write(b)
			}
		}

		and:
		def response = new ServletResponseImpl(servletResponse)

		when: "the response is written to"
		response.outputStream.withStream {
			it << "O HAI! \u00a31 KTHXBYE".getBytes("ISO-8859-1")
		}

		then: "the content can be read back as binary data"
		response.hasBody()
		response.bodyAsBinary.bytes == "O HAI! \u00a31 KTHXBYE".getBytes("ISO-8859-1")

		and: "the underlying servlet response is written to"
		servletOutputStream.toByteArray() == "O HAI! \u00a31 KTHXBYE".getBytes("ISO-8859-1")
	}

	@Unroll("#encoding encoded response body with #charset charset can be written to and read from")
	void "encoded response body can be written to and read from"() {
		given: "the underlying servlet response output stream"
		def servletOutputStream = new ByteArrayOutputStream()
		servletResponse.getOutputStream() >> new ServletOutputStream() {
			@Override
			void write(int b) {
				servletOutputStream.write(b)
			}
		}

		and: "the underlying response charset has been set"
		servletResponse.getCharacterEncoding() >> charset

		and:
		def response = new ServletResponseImpl(servletResponse)

		when: "the response is written to"
		response.addHeader("Content-Encoding", encoding)
		response.writer.withWriter {
			it << "O HAI! \u00a31 KTHXBYE"
		}

		then: "the content can be read back as encoded binary data"
		response.bodyAsBinary.bytes == encoder.encode("O HAI! \u00a31 KTHXBYE", charset)

		then: "the content can be read back as text"
		response.bodyAsText.text == "O HAI! \u00a31 KTHXBYE"

		and: "the underlying servlet response is written to"
		servletOutputStream.toByteArray() == encoder.encode("O HAI! \u00a31 KTHXBYE", charset)

		where:
		encoding  | encoder              | charset
		"gzip"    | new GzipEncoder()    | "ISO-8859-1"
		"deflate" | new DeflateEncoder() | "ISO-8859-1"
		"gzip"    | new GzipEncoder()    | "UTF-8"
		"deflate" | new DeflateEncoder() | "UTF-8"
	}

}
