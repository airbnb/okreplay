package co.freeside.betamax.httpclient

import co.freeside.betamax.Recorder
import co.freeside.betamax.handler.*
import co.freeside.betamax.message.httpclient.HttpRequestAdapter
import org.apache.http.*
import org.apache.http.client.RequestDirector
import org.apache.http.entity.*
import org.apache.http.impl.EnglishReasonPhraseCatalog
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicHttpResponse
import org.apache.http.protocol.HttpContext

class BetamaxRequestDirector implements RequestDirector {

	private final RequestDirector delegate
	private final HttpHandler handlerChain

	BetamaxRequestDirector(RequestDirector delegate, Recorder recorder) {
		this.delegate = delegate

		handlerChain = new TapeReader(recorder)
		handlerChain <<
				new TapeWriter(recorder) <<
				new HeaderFilter() <<
				new TargetConnector(new DefaultHttpClient())
	}

	@Override
	HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) {
		def requestWrapper = new HttpRequestAdapter(request)
		def responseWrapper = handlerChain.handle(requestWrapper)

		def response = new BasicHttpResponse(
				HttpVersion.HTTP_1_1,
				responseWrapper.status,
				EnglishReasonPhraseCatalog.INSTANCE.getReason(responseWrapper.status, Locale.ENGLISH)
		)
		responseWrapper.headers.each { name, value ->
			value.tokenize(',').each {
				response.addHeader(name, it.trim())
			}
		}
		response.addHeader(HttpHeaders.VIA, 'Betamax')
		if (responseWrapper.hasBody()) {
			response.entity = new ByteArrayEntity(responseWrapper.bodyAsBinary.bytes, ContentType.create(responseWrapper.contentType))
		}
		response
	}

}
