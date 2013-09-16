package co.freeside.betamax.proxy

import co.freeside.betamax.proxy.netty.NettyRequestAdapter
import co.freeside.betamax.tape.Tape
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.*
import org.littleshoot.proxy.HttpFiltersAdapter
import static co.freeside.betamax.Headers.VIA_HEADER
import static io.netty.handler.codec.http.HttpHeaders.Names.VIA
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1

class BetamaxFilters extends HttpFiltersAdapter {

	private final Tape tape

	BetamaxFilters(HttpRequest originalRequest, Tape tape) {
		super(originalRequest)
		this.tape = tape
	}

	@Override
	HttpResponse requestPre(HttpObject httpObject) {
		println "requestPre ${httpObject.getClass().name}"

		FullHttpResponse response = null

		if (httpObject instanceof HttpRequest) {
			def request = NettyRequestAdapter.wrap(httpObject)
			if (tape.seek(request)) {
				def recordedResponse = tape.play(request)
				def status = HttpResponseStatus.valueOf(recordedResponse.status)
				def content = recordedResponse.hasBody() ? Unpooled.copiedBuffer(recordedResponse.bodyAsBinary.bytes) : Unpooled.EMPTY_BUFFER
				response = recordedResponse.hasBody() ? new DefaultFullHttpResponse(HTTP_1_1, status, content) : new DefaultFullHttpResponse(HTTP_1_1, status)
				for (Map.Entry<String, String> header : recordedResponse.headers) {
					response.headers().set(header.key, header.value.split(/,\s*/).toList())
				}
			}
		}

		return response
	}

	@Override
	HttpResponse requestPost(HttpObject httpObject) {
		println "requestPost ${httpObject.getClass().name}"
	}

	@Override
	void responsePre(HttpObject httpObject) {
		println "responsePre ${httpObject.getClass().name}"
	}

	@Override
	void responsePost(HttpObject httpObject) {
		println "responsePost ${httpObject.getClass().name}"
		if (httpObject instanceof HttpResponse) {
			def response = (HttpResponse) httpObject
			response.headers().set(VIA, VIA_HEADER)
		}
	}
}
