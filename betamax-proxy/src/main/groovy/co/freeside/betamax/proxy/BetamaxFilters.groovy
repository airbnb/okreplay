package co.freeside.betamax.proxy

import java.util.logging.Logger
import co.freeside.betamax.handler.NonWritableTapeException
import co.freeside.betamax.message.Request
import co.freeside.betamax.proxy.netty.NettyRequestAdapter
import co.freeside.betamax.proxy.netty.NettyResponseAdapter
import co.freeside.betamax.tape.Tape
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.*
import org.littleshoot.proxy.HttpFiltersAdapter
import static co.freeside.betamax.Headers.*
import static io.netty.handler.codec.http.HttpHeaders.Names.VIA
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import static java.util.logging.Level.INFO

class BetamaxFilters extends HttpFiltersAdapter {

	private final Request request
	private final Tape tape
	private static final Logger log = Logger.getLogger(BetamaxFilters.class.getName());

	BetamaxFilters(HttpRequest originalRequest, Tape tape) {
		super(originalRequest)
		this.request = NettyRequestAdapter.wrap(originalRequest)
		this.tape = tape
	}

	@Override
	HttpResponse requestPre(HttpObject httpObject) {
		println "requestPre ${httpObject.getClass().name}"

		FullHttpResponse response = null

		if (httpObject instanceof HttpRequest) {
			if (tape.isReadable() && tape.seek(request)) {
				def recordedResponse = tape.play(request)
				def status = HttpResponseStatus.valueOf(recordedResponse.status)
				def content = recordedResponse.hasBody() ? Unpooled.copiedBuffer(recordedResponse.bodyAsBinary.bytes) : Unpooled.EMPTY_BUFFER
				response = recordedResponse.hasBody() ? new DefaultFullHttpResponse(HTTP_1_1, status, content) : new DefaultFullHttpResponse(HTTP_1_1, status)
				for (Map.Entry<String, String> header : recordedResponse.headers) {
					response.headers().set(header.key, header.value.split(/,\s*/).toList())
				}

				response.headers().add(X_BETAMAX, "PLAY")
			}
		}

		return response
	}

	@Override
	HttpResponse requestPost(HttpObject httpObject) {
		println "requestPost ${httpObject.getClass().name}"

		if (httpObject instanceof HttpRequest) {
			((HttpRequest)httpObject).headers().set(VIA, "Betamax")
		}

		return null;
	}

	@Override
	void responsePre(HttpObject httpObject) {
		println "responsePre ${httpObject.getClass().name}"

		if (httpObject instanceof FullHttpResponse) {
			FullHttpResponse nettyResponse = httpObject
			if (!tape.isWritable()) {
				throw new NonWritableTapeException();
			}

			log.log(INFO, "Recording to '" + tape.getName() + "'");
			tape.record(request, NettyResponseAdapter.wrap(nettyResponse));

			nettyResponse.headers().add(X_BETAMAX, "REC");
		}
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
