package betamax.proxy.httpcore

import org.apache.http.entity.ByteArrayEntity
import betamax.encoding.*
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.client.methods.*
import org.apache.http.message.*
import spock.lang.*
import org.apache.commons.codec.StringEncoder
import org.apache.http.entity.StringEntity
import org.apache.http.entity.BasicHttpEntity

class HttpCoreMessageImplSpec extends Specification {

	HttpRequest getRequest = new HttpGet("http://robfletcher.github.com/betamax")
	HttpEntityEnclosingRequest postRequest = new HttpPost("http://robfletcher.github.com/betamax")
	HttpResponse successResponse = new BasicHttpResponse(HTTP_1_1, 200, "OK")

	def "request can read basic fields"() {
		given:
		def request = new HttpCoreRequestImpl(getRequest)

		expect:
		request.method == "GET"
		request.target == new URI("http://robfletcher.github.com/betamax")
	}

	def "request can read headers"() {
		given:
		getRequest.addHeader(IF_NONE_MATCH, "abc123")
		getRequest.addHeader(ACCEPT_ENCODING, "gzip")
		getRequest.addHeader(ACCEPT_ENCODING, "deflate")

		and:
		def request = new HttpCoreRequestImpl(getRequest)

		expect:
		request.getFirstHeader(IF_NONE_MATCH) == "abc123"
		request.getHeaders(ACCEPT_ENCODING) == ["gzip", "deflate"]
	}

	def "request headers are immutable"() {
		given:
		def request = new HttpCoreRequestImpl(getRequest)

		when:
		request.headers[IF_NONE_MATCH] = ["abc123"]

		then:
		thrown UnsupportedOperationException
	}

	def "request can add headers"() {
		given:
		getRequest.addHeader(ACCEPT_ENCODING, "gzip")

		and:
		def request = new HttpCoreRequestImpl(getRequest)

		when:
		request.addHeader(IF_NONE_MATCH, "abc123")
		request.addHeader(ACCEPT_ENCODING, "deflate")

		then:
		getRequest.getFirstHeader(IF_NONE_MATCH).value == "abc123"
		getRequest.getHeaders(ACCEPT_ENCODING)*.value == ["gzip", "deflate"]
	}

	def "request body is not readable if there is no body"() {
		given:
		def request = new HttpCoreRequestImpl(getRequest)

		when:
		request.bodyAsText

		then:
		thrown UnsupportedOperationException
	}

	def "request body is readable as text"() {
		given:
		postRequest.entity = new ByteArrayEntity("value=£1".getBytes("ISO-8859-1"))
		postRequest.entity.contentType = new BasicHeader(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=ISO-8859-1")

		and:
		def request = new HttpCoreRequestImpl(postRequest)

		expect:
		request.bodyAsText.text == "value=£1"
	}

	def "request body is readable as binary"() {
		given:
		postRequest.entity = new ByteArrayEntity("value=£1".getBytes("ISO-8859-1"))
		postRequest.entity.contentType = new BasicHeader(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=ISO-8859-1")

		and:
		def request = new HttpCoreRequestImpl(postRequest)

		expect:
		request.bodyAsBinary.text == "value=£1"
	}

	def "response can read basic fields"() {
		given:
		def response = new HttpCoreResponseImpl(successResponse)

		expect:
		response.status == 200
		response.reason == "OK"
	}

	def "response can read headers"() {
		given:
		successResponse.addHeader(ETAG, "abc123")
		successResponse.addHeader(VARY, "Content-Language")
		successResponse.addHeader(VARY, "Content-Type")

		and:
		def response = new HttpCoreResponseImpl(successResponse)

		expect:
		response.getFirstHeader(ETAG) == "abc123"
		response.getHeaders(VARY) == ["Content-Language", "Content-Type"]
	}

	def "response headers are immutable"() {
		given:
		def response = new HttpCoreResponseImpl(successResponse)

		when:
		response.headers[ETAG] = ["abc123"]

		then:
		thrown UnsupportedOperationException
	}

	def "response can add headers"() {
		given:
		successResponse.addHeader(VARY, "Content-Language")

		and:
		def response = new HttpCoreResponseImpl(successResponse)

		when:
		response.addHeader(ETAG, "abc123")
		response.addHeader(VARY, "Content-Type")

		then:
		successResponse.getFirstHeader(ETAG).value == "abc123"
		successResponse.getHeaders(VARY)*.value == ["Content-Language", "Content-Type"]
	}

	def "response body is not readable if there is no body"() {
		given:
		def response = new HttpCoreResponseImpl(successResponse)

		when:
		response.bodyAsText

		then:
		thrown UnsupportedOperationException
	}

	def "response body is readable as text"() {
		given:
		successResponse.entity = new ByteArrayEntity("O HAI! £1 KTHXBYE".getBytes("ISO-8859-1"))
		successResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")

		and:
		def response = new HttpCoreResponseImpl(successResponse)

		expect:
		response.bodyAsText.text == "O HAI! £1 KTHXBYE"
	}

	def "response body is readable as binary"() {
		given:
		successResponse.entity = new ByteArrayEntity("O HAI! £1 KTHXBYE".getBytes("ISO-8859-1"))
		successResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")

		and:
		def response = new HttpCoreResponseImpl(successResponse)

		expect:
		response.bodyAsBinary.text == "O HAI! £1 KTHXBYE"
	}

	def "response body can be re-read even if underlying entity is not repeatable"() {
		given:
		successResponse.entity = new BasicHttpEntity(content: new ByteArrayInputStream("O HAI! £1 KTHXBYE".getBytes("ISO-8859-1")))
		successResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")

		and:
		def response = new HttpCoreResponseImpl(successResponse)


		expect:
		response.bodyAsBinary.text == "O HAI! £1 KTHXBYE"
		response.bodyAsBinary.text == "O HAI! £1 KTHXBYE"
	}

	@Unroll("#encoding encoded response body is not decoded when read as binary")
	def "encoded response body is not decoded when read as binary"() {
		given:
		successResponse.entity = new ByteArrayEntity(encoder.encode("O HAI! £1 KTHXBYE", "ISO-8859-1"))
		successResponse.entity.contentType = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")
		successResponse.entity.contentEncoding = new BasicHeader(CONTENT_ENCODING, encoding)

		and:
		def response = new HttpCoreResponseImpl(successResponse)

		expect:
		response.bodyAsBinary.bytes == successResponse.entity.content.bytes

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

	@Unroll("#encoding encoded response body is readable as text")
	def "encoded response body is readable as text"() {
		given:
		def contentTypeHeader = new BasicHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")
		def encodingHeader = new BasicHeader(CONTENT_ENCODING, encoding)
		successResponse.addHeader(contentTypeHeader)
		successResponse.addHeader(encodingHeader)
		successResponse.entity = new ByteArrayEntity(encoder.encode("O HAI! £1 KTHXBYE", "ISO-8859-1"))
		successResponse.entity.contentType = contentTypeHeader
		successResponse.entity.contentEncoding = encodingHeader

		and:
		def response = new HttpCoreResponseImpl(successResponse)

		expect:
		response.bodyAsText.text == "O HAI! £1 KTHXBYE"

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

	def "can write to response body"() {
		given:
		def response = new HttpCoreResponseImpl(successResponse)
		response.addHeader(CONTENT_TYPE, "text/plain;charset=ISO-8859-1")

		when:
		response.writer.withWriter {
			it << "O HAI! £1 KTHXBYE"
		}

		then:
		successResponse.entity.content.bytes == "O HAI! £1 KTHXBYE".getBytes("ISO-8859-1")
	}

	@Unroll("response body is #encoding encoded when written")
	def "response body is encoded when written"() {
		given:
		def response = new HttpCoreResponseImpl(successResponse)
		response.addHeader(CONTENT_ENCODING, encoding)

		when:
		response.writer.withWriter {
			it << "O HAI! £1 KTHXBYE"
		}

		then:
		successResponse.entity.content.bytes == encoder.encode("O HAI! £1 KTHXBYE")

		where:
		encoding  | encoder
		"gzip"    | new GzipEncoder()
		"deflate" | new DeflateEncoder()
	}

	def "cannot write to response body if the response already has an entity"() {
		given:
		successResponse.entity = new StringEntity("KTHXBYE", "text/plain", "ISO-8859-1")

		def response = new HttpCoreResponseImpl(successResponse)

		when:
		response.writer << "O HAI! £1 KTHXBYE"

		then:
		thrown IllegalStateException
	}

}
