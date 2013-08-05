package co.freeside.betamax.proxy.netty

import co.freeside.betamax.handler.*
import co.freeside.betamax.message.*
import co.freeside.betamax.util.message.BasicResponse
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.codec.http.*
import spock.lang.Specification
import static io.netty.handler.codec.http.HttpHeaders.Names.ETAG
import static io.netty.handler.codec.http.HttpMethod.GET
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import static io.netty.handler.codec.rtsp.RtspHeaders.Names.VIA

class BetamaxChannelHandlerSpec extends Specification {

	BetamaxChannelHandler proxy = new BetamaxChannelHandler()
	FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, "http://freeside.co/betamax/")
	FullHttpResponse response = null
	Response betamaxResponse
	ChannelHandlerContext context = Stub(ChannelHandlerContext) {
		writeAndFlush(_) >> {
			response = it[0]
			return new DefaultChannelPromise(context.channel())
		}
	}

	void setup() {
		betamaxResponse = new BasicResponse(200, 'OK')
		betamaxResponse.addHeader(ETAG, UUID.randomUUID().toString())
		betamaxResponse.addHeader(VIA, 'Proxy 1, Proxy 2')
		betamaxResponse.body = 'O HAI'.bytes
	}

	void 'passes the request to its handler chain'() {
		given:
		def handler = Mock(HttpHandler)
		proxy << handler

		when:
		proxy.channelRead(context, request)

		then:
		1 * handler.handle(_) >> { Request wrappedRequest ->
			assert wrappedRequest instanceof NettyRequestAdapter
			assert wrappedRequest.originalRequest.is(request)
			betamaxResponse
		}
	}

	void 'populates the response with whatever comes back from the handler chain'() {
		given:
		def handler = Mock(HttpHandler)
		handler.handle(_) >> betamaxResponse
		proxy << handler

		when:
		proxy.channelRead(context, request)

		then:
		response.status.code() == betamaxResponse.status
		response.headers().get(ETAG) == betamaxResponse.getHeader(ETAG)
		response.headers().getAll(VIA).containsAll(betamaxResponse.getHeader(VIA).split(/,\s*/))
		response.content() == Unpooled.copiedBuffer(betamaxResponse.bodyAsBinary.bytes)
	}

	void 'responds with the specified error status if the handler chain throws ProxyException'() {
		when:
		proxy.exceptionCaught context, [
				getHttpStatus: {-> errorStatus},
				getMessage: {-> errorMessage}
		] as HandlerException

		then:
		response.status.code() == errorStatus
		response.status.reasonPhrase() == errorMessage

		where:
		errorStatus = 419
		errorMessage = "the error message"
	}

	void 'responds with HTTP 500 if the handler chain throws any other exception'() {
		when:
		proxy.exceptionCaught context, new IllegalStateException()

		then:
		response.status == INTERNAL_SERVER_ERROR
	}

}
