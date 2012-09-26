package co.freeside.betamax.httpclient

import co.freeside.betamax.Recorder
import co.freeside.betamax.message.http.HttpRequestAdapter
import co.freeside.betamax.proxy.handler.*
import org.apache.http.*
import org.apache.http.client.*
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.entity.*
import org.apache.http.impl.EnglishReasonPhraseCatalog
import org.apache.http.message.BasicHttpResponse
import org.apache.http.params.HttpParams
import org.apache.http.protocol.HttpContext
import static java.util.Locale.ENGLISH
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpVersion.HTTP_1_1

class BetamaxHttpClient implements HttpClient {

	private final HttpClient delegate
	private final HttpHandler handlerChain

	BetamaxHttpClient(HttpClient delegate, Recorder recorder) {
		this.delegate = delegate
		handlerChain = new TapeReader(recorder)
		handlerChain <<
				new TapeWriter(recorder) <<
				new HeaderFilter() <<
				new TargetConnector(delegate)
	}

	@Override
	HttpParams getParams() {
		delegate.params
	}

	@Override
	ClientConnectionManager getConnectionManager() {
		delegate.connectionManager
	}

	@Override
	HttpResponse execute(HttpUriRequest request) {
		def requestWrapper = new HttpRequestAdapter(request)
		def responseWrapper = handlerChain.handle(requestWrapper)

		def response = new BasicHttpResponse(
				HTTP_1_1,
				responseWrapper.status,
				EnglishReasonPhraseCatalog.INSTANCE.getReason(responseWrapper.status, ENGLISH)
		)
		responseWrapper.headers.each { name, value ->
			value.tokenize(',').each {
				response.addHeader(name, it.trim())
			}
		}
		response.addHeader(VIA, 'Betamax')
		if (responseWrapper.hasBody()) {
			response.entity = new ByteArrayEntity(responseWrapper.bodyAsBinary.bytes, ContentType.create(responseWrapper.contentType))
		}
		response
	}

	@Override
	HttpResponse execute(HttpUriRequest request, HttpContext context) {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	HttpResponse execute(HttpHost target, HttpRequest request) {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	def <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	def <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	def <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	def <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) {
		return null  //To change body of implemented methods use File | Settings | File Templates.
	}
}
