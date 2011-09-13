package betamax.proxy.servlet

import betamax.util.servlet.MockHttpServletRequest
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse
import betamax.encoding.*
import spock.lang.*

class ServletMessageImplSpec extends Specification {

	MockHttpServletRequest getRequest = new MockHttpServletRequest(method: "GET")
	MockHttpServletRequest postRequest = new MockHttpServletRequest(method: "POST")
	HttpServletResponse servletResponse = Mock(HttpServletResponse)

	def "request can read basic fields"() {
		given:
		def request = new ServletRequestImpl(getRequest)

		expect:
		request.method == "GET"
		request.target == new URI("http://robfletcher.github.com/betamax")
	}

	def "request can read headers"() {
		given:
		getRequest.setHeader("If-None-Match", "abc123")
		getRequest.addHeader("Accept-Encoding", "gzip")
		getRequest.addHeader("Accept-Encoding", "deflate")

		and:
		def request = new ServletRequestImpl(getRequest)

		expect:
		request.getFirstHeader("If-None-Match") == "abc123"
		request.getHeaders("Accept-Encoding") == ["gzip", "deflate"]
	}

	def "request headers are immutable"() {
		given:
		def request = new ServletRequestImpl(getRequest)

		when:
		request.headers["If-None-Match"] = ["abc123"]

		then:
		thrown UnsupportedOperationException
	}

	def "request can add headers"() {
		given:
		getRequest.addHeader("Accept-Encoding", "gzip")

		and:
		def request = new ServletRequestImpl(getRequest)

		when:
		request.addHeader("If-None-Match", "abc123")
		request.addHeader("Accept-Encoding", "deflate")

		then:
		getRequest.getHeader("If-None-Match") == "abc123"
		getRequest.getHeader("Accept-Encoding") == "gzip, deflate"
	}

	def "request body is not readable if there is no body"() {
		given:
		def request = new ServletRequestImpl(getRequest)

		when:
		request.bodyAsText

		then:
		thrown UnsupportedOperationException
	}

	def "request body is readable as text"() {
		given:
		postRequest.body = "value=\u00a31".getBytes("ISO-8859-1")
		postRequest.contentType = "application/x-www-form-urlencoded"
		postRequest.characterEncoding = "ISO-8859-1"

		and:
		def request = new ServletRequestImpl(postRequest)

		expect:
		request.bodyAsText.text == "value=\u00a31"
	}

	def "request body is readable as binary"() {
		given:
		postRequest.body = "value=\u00a31".getBytes("ISO-8859-1")
		postRequest.contentType = "application/x-www-form-urlencoded"
		postRequest.characterEncoding = "ISO-8859-1"

		and:
		def request = new ServletRequestImpl(postRequest)

		expect:
		request.bodyAsBinary.bytes == "value=\u00a31".getBytes("ISO-8859-1")
	}

	def "response can read basic fields"() {
		given:
		def response = new ServletResponseImpl(servletResponse)

		when: "status and reason are set"
		response.status = 200
		response.reason = "OK"

		then: "they can be retrieved again"
		response.status == 200
		response.reason == "OK"

		and: "the underlying servlet response values are set"
		1 * servletResponse.setStatus(200)
		1 * servletResponse.setMessage("OK")
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

	def "response body is not readable if there is no body"() {
		given:
		def response = new ServletResponseImpl(servletResponse)

		when:
		response.bodyAsText

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
