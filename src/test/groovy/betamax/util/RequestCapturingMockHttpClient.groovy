package betamax.util

import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.message.BasicHttpResponse
import org.apache.http.params.HttpParams
import org.apache.http.protocol.HttpContext
import org.apache.http.*
import org.apache.http.client.*

class RequestCapturingMockHttpClient implements HttpClient {

	private final Stack<HttpRequest> requests = new Stack<HttpRequest>()

	HttpRequest getLastRequest() {
		requests.peek()
	}

	HttpParams getParams() {
		null
	}

	ClientConnectionManager getConnectionManager() {
		null
	}

	HttpResponse execute(HttpUriRequest request) {
		requests << request
		new BasicHttpResponse(org.apache.http.HttpVersion.HTTP_1_1, 200, "OK")
	}

	HttpResponse execute(HttpUriRequest request, HttpContext context) {
		execute(request)
	}

	HttpResponse execute(HttpHost target, HttpRequest request) {
		throw new UnsupportedOperationException()
	}

	HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) {
		throw new UnsupportedOperationException()
	}

	def <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) {
		throw new UnsupportedOperationException()
	}

	def <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) {
		throw new UnsupportedOperationException()
	}

	def <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) {
		throw new UnsupportedOperationException()
	}

	def <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) {
		throw new UnsupportedOperationException()
	}

}
