package betamax.proxy.servlet

import betamax.encoding.*
import betamax.util.servlet.*
import static javax.servlet.http.HttpServletResponse.SC_OK
import spock.lang.*

class ServletMessageImplSpec extends Specification {

	MockHttpServletRequest getRequest = new MockHttpServletRequest(method: "GET")
	MockHttpServletRequest postRequest = new MockHttpServletRequest(method: "POST")
	MockHttpServletResponse successResponse = new MockHttpServletResponse(status: SC_OK, message: "OK")

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
		def response = new ServletResponseImpl(successResponse)

		expect:
		response.status == 200
		response.reason == "OK"
	}

	def "response can read headers"() {
		given:
		successResponse.addHeader("E-Tag", "abc123")
		successResponse.addHeader("Vary", "Content-Language")
		successResponse.addHeader("Vary", "Content-Type")

		and:
		def response = new ServletResponseImpl(successResponse)

		expect:
		response.getFirstHeader("E-Tag") == "abc123"
		response.getHeaders("Vary") == ["Content-Language", "Content-Type"]
	}

	def "response headers are immutable"() {
		given:
		def response = new ServletResponseImpl(successResponse)

		when:
		response.headers["E-Tag"] = ["abc123"]

		then:
		thrown UnsupportedOperationException
	}

	def "response can add headers"() {
		given:
		successResponse.addHeader("Vary", "Content-Language")

		and:
		def response = new ServletResponseImpl(successResponse)

		when:
		response.addHeader("E-Tag", "abc123")
		response.addHeader("Vary", "Content-Type")

		then:
		successResponse.getHeader("E-Tag") == "abc123"
		successResponse.getHeader("Vary") == "Content-Language, Content-Type"
	}

	def "response body is not readable if there is no body"() {
		given:
		def response = new ServletResponseImpl(successResponse)

		when:
		response.bodyAsText

		then:
		thrown UnsupportedOperationException
	}

	def "response body is readable as text"() {
		given:
		successResponse.body = "O HAI! \u00a31 KTHXBYE".getBytes("ISO-8859-1")
		successResponse.characterEncoding = "ISO-8859-1"

		and:
		def response = new ServletResponseImpl(successResponse)

		expect:
		response.bodyAsText.text == "O HAI! \u00a31 KTHXBYE"
	}

	def "response body is readable as binary"() {
		given:
		successResponse.body = "O HAI! \u00a31 KTHXBYE".getBytes("ISO-8859-1")
		successResponse.characterEncoding = "ISO-8859-1"

		and:
		def response = new ServletResponseImpl(successResponse)

		expect:
		response.bodyAsBinary.text == "O HAI! \u00a31 KTHXBYE"
	}

	def "response body can be re-read even if underlying entity is not repeatable"() {
		given:
		successResponse.body = "O HAI! \u00a31 KTHXBYE".getBytes("ISO-8859-1")
		successResponse.characterEncoding = "ISO-8859-1"

		and:
		def response = new ServletResponseImpl(successResponse)


		expect:
		response.bodyAsBinary.text == "O HAI! \u00a31 KTHXBYE"
		response.bodyAsBinary.text == "O HAI! \u00a31 KTHXBYE"
	}

	@Unroll("#encoding encoded response body is not decoded when read as binary")
	def "encoded response body is not decoded when read as binary"() {
		given:
		successResponse.body = encoder.encode("O HAI! \u00a31 KTHXBYE", "ISO-8859-1")
		successResponse.contentType = "text/plain;charset=ISO-8859-1"
		successResponse.addHeader("Content-Encoding", encoding)

		and:
		def response = new ServletResponseImpl(successResponse)

		expect:
		response.bodyAsBinary.bytes == successResponse.body

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

	@Unroll("#encoding encoded response body is readable as text")
	def "encoded response body is readable as text"() {
		given:
		successResponse.body = encoder.encode("O HAI! \u00a31 KTHXBYE", "ISO-8859-1")
		successResponse.contentType = "text/plain;charset=ISO-8859-1"
		successResponse.addHeader("Content-Encoding", encoding)

		and:
		def response = new ServletResponseImpl(successResponse)

		expect:
		response.bodyAsText.text == "O HAI! \u00a31 KTHXBYE"

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

	def "can write to response body"() {
		given:
		def response = new ServletResponseImpl(successResponse)
		response.addHeader("Content-Type", "text/plain;charset=ISO-8859-1")

		when:
		response.writer.withWriter {
			it << "O HAI! \u00a31 KTHXBYE"
		}

		then:
		successResponse.body == "O HAI! \u00a31 KTHXBYE".getBytes("ISO-8859-1")
	}

	@Unroll("response body is #encoding encoded when written")
	def "response body is encoded when written"() {
		given:
		def response = new ServletResponseImpl(successResponse)
		response.addHeader("Content-Encoding", encoding)

		when:
		response.writer.withWriter {
			it << "O HAI! \u00a31 KTHXBYE"
		}

		then:
		successResponse.body == encoder.encode("O HAI! \u00a31 KTHXBYE")

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

	def "cannot write to response body if the response already has an entity"() {
		given:
		successResponse.body = "KTHXBYE".getBytes("ISO-8859-1")
		successResponse.characterEncoding = "ISO-8859-1"

		def response = new ServletResponseImpl(successResponse)

		when:
		response.writer << "O HAI! \u00a31 KTHXBYE"

		then:
		thrown IllegalStateException
	}

}
