package co.freeside.betamax.proxy.handler

import co.freeside.betamax.message.Request
import co.freeside.betamax.message.Response
import co.freeside.betamax.message.http.HttpResponseAdapter
import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestFactory
import org.apache.http.client.HttpClient
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.DefaultHttpRequestFactory

import static co.freeside.betamax.proxy.jetty.BetamaxProxy.VIA_HEADER
import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY
import static java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT
import static org.apache.http.HttpHeaders.VIA

class TargetConnector implements HttpHandler {

	private final HttpClient httpClient
	private final HttpRequestFactory httpRequestFactory = new DefaultHttpRequestFactory()

	TargetConnector(HttpClient httpClient) {
		this.httpClient = httpClient
	}

	Response handle(Request request) {
		def outboundRequest = createOutboundRequest(request)
		def httpHost = new HttpHost(request.uri.host, request.uri.port, request.uri.scheme)

		try {
			def response = httpClient.execute(httpHost, outboundRequest)
			new HttpResponseAdapter(response)
		} catch (SocketTimeoutException e) {
			throw new ProxyException(HTTP_GATEWAY_TIMEOUT, "Timed out connecting to $request.uri", e)
		} catch (IOException e) {
			throw new ProxyException(HTTP_BAD_GATEWAY, "Problem connecting to $request.uri", e)
		}
	}

	private HttpRequest createOutboundRequest(Request request) {
		def outboundRequest = httpRequestFactory.newHttpRequest(request.method, request.uri.toString())
		request.headers.each { name, value ->
			outboundRequest.addHeader(name, value)
		}
		outboundRequest.addHeader(VIA, VIA_HEADER)
		if (request.hasBody()) {
			outboundRequest.entity = new ByteArrayEntity(request.bodyAsBinary.bytes)
		}
		outboundRequest
	}

}
