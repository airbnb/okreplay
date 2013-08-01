package co.freeside.betamax.proxy.netty

import co.freeside.betamax.handler.*
import co.freeside.betamax.message.*
import co.freeside.betamax.message.servlet.NettyRequestAdapter
import co.freeside.betamax.util.message.BasicResponse
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import spock.lang.Specification
import static io.netty.handler.codec.http.HttpHeaders.Names.ETAG
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR
import static io.netty.handler.codec.rtsp.RtspHeaders.Names.VIA

class BetamaxProxySpec extends Specification {

	BetamaxChannelHandler proxy = new BetamaxChannelHandler()

	HttpRequest request = [:] as HttpRequest

	Response betamaxResponse

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

		and:
		def context = Stub(ChannelHandlerContext)

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

		and:
		def context = Mock(ChannelHandlerContext)

		when:
		proxy.channelRead(context, request)

		then:
		1 * context.writeAndFlush(_) >> { FullHttpResponse response ->
			response.status.code() == betamaxResponse.status
			response.headers().get(ETAG) == betamaxResponse.getHeader(ETAG)
			response.headers().getAll(VIA).containsAll(betamaxResponse.getHeader(VIA).split(/,\s*/))
			response.content() == Unpooled.copiedBuffer(betamaxResponse.bodyAsBinary.bytes)
		}
	}

	void 'responds with the specified error status if the handler chain throws ProxyException'() {
		given:
		def handler = Mock(HttpHandler)
		handler.handle(_) >> { throw [getHttpStatus: {-> errorStatus}] as HandlerException }
		proxy << handler

		and:
		def context = Mock(ChannelHandlerContext)

		when:
		proxy.channelRead(context, request)

		then:
		1 * context.writeAndFlush(_) >> { FullHttpResponse response ->
			response.status.code() == errorStatus
		}

		where:
		errorStatus = 419
	}

	void 'responds with HTTP 500 if the handler chain throws any other exception'() {
		given:
		def handler = Mock(HttpHandler)
		handler.handle(_) >> { throw new IllegalStateException() }
		proxy << handler

		and:
		def context = Mock(ChannelHandlerContext)

		when:
		proxy.channelRead(context, request)

		then:
		1 * context.writeAndFlush(_) >> { FullHttpResponse response ->
			response.status == INTERNAL_SERVER_ERROR
		}
	}

}
