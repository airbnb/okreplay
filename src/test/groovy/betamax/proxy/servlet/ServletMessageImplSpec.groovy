package betamax.proxy.servlet

import javax.servlet.ServletOutputStream
import betamax.encoding.*
import javax.servlet.http.*
import spock.lang.*
import betamax.util.servlet.MockServletInputStream
import org.apache.commons.collections.iterators.IteratorEnumeration

class ServletMessageImplSpec extends Specification {

	HttpServletRequest servletRequest = Mock(HttpServletRequest)
	HttpServletResponse servletResponse = Mock(HttpServletResponse)

	def "request can read basic fields"() {
		given:
		servletRequest.getMethod() >> "GET"
		servletRequest.getRequestURL() >> new StringBuffer("http://robfletcher.github.com/betamax")

		and:
		def request = new ServletRequestImpl(servletRequest)

		expect:
		request.method == "GET"
		request.target == new URI("http://robfletcher.github.com/betamax")
	}

	def "request target includes query string"() {
		given:
		servletRequest.getRequestURL() >> new StringBuffer("http://robfletcher.github.com/betamax")
		servletRequest.getQueryString() >> "q=1"

		and:
		def request = new ServletRequestImpl(servletRequest)

		expect:
		request.target == new URI("http://robfletcher.github.com/betamax?q=1")
	}

	def "request can read headers"() {
		given:
		servletRequest.getHeaderNames() >> new IteratorEnumeration(["If-None-Match", "Accept-Encoding"].iterator())
		servletRequest.getHeaders("If-None-Match") >> new IteratorEnumeration(["abc123"].iterator())
		servletRequest.getHeaders("Accept-Encoding") >> new IteratorEnumeration(["gzip", "deflate"].iterator())

		and:
		def request = new ServletRequestImpl(servletRequest)

		expect:
		request.getFirstHeader("If-None-Match") == "abc123"
		request.getHeaders("Accept-Encoding") == ["gzip", "deflate"]
	}

	def "request headers are immutable"() {
		given:
		def request = new ServletRequestImpl(servletRequest)

		when:
		request.headers["If-None-Match"] = ["abc123"]

		then:
		thrown UnsupportedOperationException
	}

	def "request can add headers"() {
		given:
		def request = new ServletRequestImpl(servletRequest)

		when:
		request.addHeader("If-None-Match", "abc123")

		then:
		1 * servletRequest.addHeader("If-None-Match", "abc123")
	}

	def "request body is readable as text"() {
		given:
		servletRequest.getInputStream() >> new MockServletInputStream(new ByteArrayInputStream("value=\u00a31".getBytes("ISO-8859-1")))
		servletRequest.getContentType() >> "application/x-www-form-urlencoded; charset=ISO-8859-1"
		servletRequest.getCharacterEncoding() >> "ISO-8859-1"

		and:
		def request = new ServletRequestImpl(servletRequest)

		expect:
		request.bodyAsText.text == "value=\u00a31"
	}

	def "request body is readable as binary"() {
		given:
		servletRequest.getInputStream() >> new MockServletInputStream(new ByteArrayInputStream("value=\u00a31".getBytes("ISO-8859-1")))
		servletRequest.getContentType() >> "application/x-www-form-urlencoded; charset=ISO-8859-1"
		servletRequest.getCharacterEncoding() >> "ISO-8859-1"

		and:
		def request = new ServletRequestImpl(servletRequest)

		expect:
		request.bodyAsBinary.bytes == "value=\u00a31".getBytes("ISO-8859-1")
	}

	def "response can read basic fields"() {
		given:
		def response = new ServletResponseImpl(servletResponse)

		when: "status and reason are set"
		response.status = 200

		then: "they can be retrieved again"
		response.status == 200

		and: "the underlying servlet response values are set"
		1 * servletResponse.setStatus(200)
	}

	def "response can add and read headers"() {
		given:
		def response = new ServletResponseImpl(servletResponse)

		when: "headers are added"
		response.addHeader("E-Tag", "abc123")
		response.addHeader("Vary", "Content-Language")
		response.addHeader("Vary", "Content-Type")

		then: "they can be retrieved again"
		response.getFirstHeader("E-Tag") == "abc123"
		response.getHeaders("Vary") == ["Content-Language", "Content-Type"]

		and: "they are added to the underlying servlet response"
		1 * servletResponse.addHeader("E-Tag", "abc123")
		1 * servletResponse.addHeader("Vary", "Content-Language")
		1 * servletResponse.addHeader("Vary", "Content-Type")
	}

	def "content type header is handled by delegating to setContentType"() {
		def response = new ServletResponseImpl(servletResponse)

		when:
		response.addHeader("Content-Type", "text/html; charset=ISO-8859-1")

		then:
		1 * servletResponse.setContentType("text/html; charset=ISO-8859-1")
		0 * servletResponse.addHeader(_, _)

		and:
		response.getFirstHeader("Content-Type") == "text/html; charset=ISO-8859-1"
	}

	def "response headers are immutable"() {
		given:
		def response = new ServletResponseImpl(servletResponse)

		when:
		response.headers["E-Tag"] = ["abc123"]

		then:
		thrown UnsupportedOperationException
	}

	@Unroll("response body can be written to and read from as #charset text")
	def "response body can be written to and read from as text"() {
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
		response.bodyAsText.text == "O HAI! \u00a31 KTHXBYE"

		and: "the underlying servlet response is written to"
		servletOutputStream.toByteArray() == "O HAI! \u00a31 KTHXBYE".getBytes(charset)

		where:
		charset << ["ISO-8859-1", "UTF-8"]
	}

	def "response body can be written to and read from as binary"() {
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
		response.bodyAsBinary.bytes == "O HAI! \u00a31 KTHXBYE".getBytes("ISO-8859-1")

		and: "the underlying servlet response is written to"
		servletOutputStream.toByteArray() == "O HAI! \u00a31 KTHXBYE".getBytes("ISO-8859-1")
	}

	@Unroll("#encoding encoded response body with #charset charset can be written to and read from")
	def "encoded response body can be written to and read from"() {
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
