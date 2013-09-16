package co.freeside.betamax.proxy.netty

import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.*
import spock.lang.*
import static io.netty.handler.codec.http.HttpHeaders.Names.*
import static io.netty.util.CharsetUtil.UTF_8

@Unroll
abstract class NettyMessageAdapterSpec<T extends HttpMessage, A extends NettyMessageAdapter<T>> extends Specification {

	@Subject A adapter
	T nettyMessage

	void 'can read headers'() {
		given:
		def headers = new DefaultHttpHeaders()
		headers.add(IF_NONE_MATCH, "abc123")
		headers.add(ACCEPT_ENCODING, ["gzip", "deflate"])
		nettyMessage.headers() >> headers

		expect:
		adapter.getHeader(IF_NONE_MATCH) == 'abc123'
		adapter.getHeader(ACCEPT_ENCODING) == 'gzip, deflate'
	}

	void 'headers are immutable'() {
		given:
		nettyMessage.headers() >> HttpHeaders.EMPTY_HEADERS

		when:
		adapter.headers[IF_NONE_MATCH] = ['abc123']

		then:
		thrown UnsupportedOperationException
	}

	void 'body is readable as text'() {
		given:
		def headers = new DefaultHttpHeaders()
		headers.set(CONTENT_TYPE, "application/x-www-form-urlencoded; charset=ISO-8859-1")
		nettyMessage.headers() >> headers

		and:
		def chunk = new DefaultHttpContent(Unpooled.copiedBuffer(bodyText.getBytes('ISO-8859-1')))
		adapter.append chunk

		expect:
		adapter.hasBody()
		adapter.bodyAsText.text == bodyText

		where:
		bodyText = "value=\u00a31"
	}

	void 'body is readable as binary'() {
		given:
		def headers = new DefaultHttpHeaders()
		headers.set(CONTENT_TYPE, "application/x-www-form-urlencoded; charset=ISO-8859-1")
		nettyMessage.headers() >> headers

		and:
		def chunk = new DefaultHttpContent(Unpooled.copiedBuffer(body))
		adapter.append chunk

		expect:
		adapter.hasBody()
		adapter.bodyAsBinary.bytes == body

		where:
		body = 'value=\u00a31'.getBytes('ISO-8859-1')
	}

	void "#description if the content buffer is #contentDescription"() {
		given:
		def chunk = new DefaultHttpContent(Unpooled.copiedBuffer(content))
		adapter.append chunk

		expect:
		adapter.hasBody() == consideredToHaveBody

		where:
		content                               | consideredToHaveBody
		Unpooled.copiedBuffer("O HAI", UTF_8) | true
		Unpooled.EMPTY_BUFFER                 | false

		description = consideredToHaveBody ? "has a body" : "does not have a body"
		contentDescription = content ? "${content.readableBytes()} bytes long" : "null"
	}

}
