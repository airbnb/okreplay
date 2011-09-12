package betamax.proxy.servlet

import betamax.encoding.*
import org.apache.http.*
import spock.lang.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.collections.EnumerationUtils
import org.apache.commons.collections.iterators.IteratorEnumeration

class ServletMessageImplSpec extends Specification {
	
//	HttpServletRequest getRequest = new HttpServletRequest("http://robfletcher.github.com/betamax")
//	HttpServletRequest postRequest = new HttpServletRequest("http://robfletcher.github.com/betamax")
//	HttpServletResponse successResponse = new HttpServletResponse(HTTP_1_1, 200, "OK")

	def "request can read basic fields"() {
		given:
		HttpServletRequest getRequest = Mock(HttpServletRequest)
		getRequest.method >> "GET"
		getRequest.requestURL >> new StringBuffer("http://robfletcher.github.com/betamax")
		def request = new ServletRequestImpl(getRequest)

		expect:
		request.method == "GET"
		request.target == new URI("http://robfletcher.github.com/betamax")
	}

	def "request can read headers"() {
		given:
		HttpServletRequest getRequest = Mock(HttpServletRequest)
		getRequest.getHeaderNames() >> new IteratorEnumeration(["If-None-Match", "Accept-Encoding"].iterator())
		getRequest.getHeaders("If-None-Match") >> new IteratorEnumeration(["abc123"].iterator())
		getRequest.getHeaders("Accept-Encoding") >> new IteratorEnumeration(["gzip", "deflate"].iterator())

		and:
		def request = new ServletRequestImpl(getRequest)

		expect:
		request.getFirstHeader("If-None-Match") == "abc123"
		request.getHeaders("Accept-Encoding") == ["gzip", "deflate"]
	}

//	def "request headers are immutable"() {
//		given:
//		def request = new ServletRequestImpl(getRequest)
//
//		when:
//		request.headers["If-None-Match"] = ["abc123"]
//
//		then:
//		thrown UnsupportedOperationException
//	}
//
//	def "request can add headers"() {
//		given:
//		getRequest.addHeader("Accept-Encoding", "gzip")
//
//		and:
//		def request = new ServletRequestImpl(getRequest)
//
//		when:
//		request.addHeader("If-None-Match", "abc123")
//		request.addHeader("Accept-Encoding", "deflate")
//
//		then:
//		getRequest.getFirstHeader("If-None-Match").value == "abc123"
//		getRequest.getHeaders("Accept-Encoding")*.value == ["gzip", "deflate"]
//	}
//
//	def "request body is not readable if there is no body"() {
//		given:
//		def request = new ServletRequestImpl(getRequest)
//
//		when:
//		request.bodyAsText
//
//		then:
//		thrown UnsupportedOperationException
//	}
//
//	def "request body is readable as text"() {
//		given:
//		postRequest.entity = new ByteArrayEntity("value=£1".getBytes("ISO-8859-1"))
//		postRequest.entity.contentType = new BasicHeader(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=ISO-8859-1")
//
//		and:
//		def request = new ServletRequestImpl(postRequest)
//
//		expect:
//		request.bodyAsText.text == "value=£1"
//	}
//
//	def "request body is readable as binary"() {
//		given:
//		postRequest.entity = new ByteArrayEntity("value=£1".getBytes("ISO-8859-1"))
//		postRequest.entity.contentType = new BasicHeader(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=ISO-8859-1")
//
//		and:
//		def request = new ServletRequestImpl(postRequest)
//
//		expect:
//		request.bodyAsBinary.text == "value=£1"
//	}
//
//	def "response can read basic fields"() {
//		given:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		expect:
//		response.status == 200
//		response.reason == "OK"
//	}
//
//	def "response can read headers"() {
//		given:
//		successResponse.addHeader(ETAG, "abc123")
//		successResponse.addHeader(VARY, "Content-Language")
//		successResponse.addHeader(VARY, "Content-Type")
//
//		and:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		expect:
//		response.getFirstHeader(ETAG) == "abc123"
//		response.getHeaders(VARY) == ["Content-Language", "Content-Type"]
//	}
//
//	def "response headers are immutable"() {
//		given:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		when:
//		response.headers[ETAG] = ["abc123"]
//
//		then:
//		thrown UnsupportedOperationException
//	}
//
//	def "response can add headers"() {
//		given:
//		successResponse.addHeader(VARY, "Content-Language")
//
//		and:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		when:
//		response.addHeader(ETAG, "abc123")
//		response.addHeader(VARY, "Content-Type")
//
//		then:
//		successResponse.getFirstHeader(ETAG).value == "abc123"
//		successResponse.getHeaders(VARY)*.value == ["Content-Language", "Content-Type"]
//	}
//
//	def "response body is not readable if there is no body"() {
//		given:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		when:
//		response.bodyAsText
//
//		then:
//		thrown UnsupportedOperationException
//	}
//
//	def "response body is readable as text"() {
//		given:
//		successResponse.entity = new ByteArrayEntity("O HAI! £1 KTHXBYE".getBytes("ISO-8859-1"))
//		successResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")
//
//		and:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		expect:
//		response.bodyAsText.text == "O HAI! £1 KTHXBYE"
//	}
//
//	def "response body is readable as binary"() {
//		given:
//		successResponse.entity = new ByteArrayEntity("O HAI! £1 KTHXBYE".getBytes("ISO-8859-1"))
//		successResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")
//
//		and:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		expect:
//		response.bodyAsBinary.text == "O HAI! £1 KTHXBYE"
//	}
//
//	def "response body can be re-read even if underlying entity is not repeatable"() {
//		given:
//		successResponse.entity = new BasicHttpEntity(content: new ByteArrayInputStream("O HAI! £1 KTHXBYE".getBytes("ISO-8859-1")))
//		successResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")
//
//		and:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//
//		expect:
//		response.bodyAsBinary.text == "O HAI! £1 KTHXBYE"
//		response.bodyAsBinary.text == "O HAI! £1 KTHXBYE"
//	}
//
//	@Unroll("#encoding encoded response body is not decoded when read as binary")
//	def "encoded response body is not decoded when read as binary"() {
//		given:
//		successResponse.entity = new ByteArrayEntity(encoder.encode("O HAI! £1 KTHXBYE", "ISO-8859-1"))
//		successResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")
//		successResponse.entity.contentEncoding = new BasicHeader(CONTENT_ENCODING, encoding)
//
//		and:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		expect:
//		response.bodyAsBinary.bytes == successResponse.entity.content.bytes
//
//		where:
//		encoding  | encoder
//		"gzip"    | new GzipEncoder()
//		"deflate" | new DeflateEncoder()
//	}
//
//	@Unroll("#encoding encoded response body is readable as text")
//	def "encoded response body is readable as text"() {
//		given:
//		def contentTypeHeader = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")
//		def encodingHeader = new BasicHeader(CONTENT_ENCODING, encoding)
//		successResponse.addHeader(contentTypeHeader)
//		successResponse.addHeader(encodingHeader)
//		successResponse.entity = new ByteArrayEntity(encoder.encode("O HAI! £1 KTHXBYE", "ISO-8859-1"))
//		successResponse.entity.contentType = contentTypeHeader
//		successResponse.entity.contentEncoding = encodingHeader
//
//		and:
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		expect:
//		response.bodyAsText.text == "O HAI! £1 KTHXBYE"
//
//		where:
//		encoding  | encoder
//		"gzip"    | new GzipEncoder()
//		"deflate" | new DeflateEncoder()
//	}
//
//	def "can write to response body"() {
//		given:
//		def response = new HttpCoreResponseImpl(successResponse)
//		response.addHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")
//
//		when:
//		response.writer.withWriter {
//			it << "O HAI! £1 KTHXBYE"
//		}
//
//		then:
//		successResponse.entity.content.bytes == "O HAI! £1 KTHXBYE".getBytes("ISO-8859-1")
//	}
//
//	@Unroll("response body is #encoding encoded when written")
//	def "response body is encoded when written"() {
//		given:
//		def response = new HttpCoreResponseImpl(successResponse)
//		response.addHeader(CONTENT_ENCODING, encoding)
//
//		when:
//		response.writer.withWriter {
//			it << "O HAI! £1 KTHXBYE"
//		}
//
//		then:
//		successResponse.entity.content.bytes == encoder.encode("O HAI! £1 KTHXBYE")
//
//		where:
//		encoding  | encoder
//		"gzip"    | new GzipEncoder()
//		"deflate" | new DeflateEncoder()
//	}
//
//	def "cannot write to response body if the response already has an entity"() {
//		given:
//		successResponse.entity = new StringEntity("KTHXBYE", "text/plain", "ISO-8859-1")
//
//		def response = new HttpCoreResponseImpl(successResponse)
//
//		when:
//		response.writer << "O HAI! £1 KTHXBYE"
//
//		then:
//		thrown IllegalStateException
//	}

}
