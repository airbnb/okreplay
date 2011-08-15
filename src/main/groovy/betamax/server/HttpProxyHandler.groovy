package betamax.server

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.*
import static org.apache.http.HttpHeaders.VIA
import org.apache.http.client.methods.*
import org.apache.http.protocol.*

class HttpProxyHandler implements HttpRequestHandler {

	private final HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager())

	void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		println "${Thread.currentThread().name}:: $request.requestLine.method request for $request.requestLine.uri"

		def proxyRequest = createProxyRequest(request)
		for (header in request.allHeaders) {
			if (proxyRequest.getHeaders(header.name).length == 0 && !(header.name in ["Proxy-Connection", "Host", "Content-Length"])) {
				proxyRequest.addHeader(header)
			}
		}
		proxyRequest.addHeader(VIA, "Betamax")

		def proxyResponse = httpClient.execute(proxyRequest)

		println "${Thread.currentThread().name}:: serving response with status $proxyResponse.statusLine.statusCode"

		response.statusCode = proxyResponse.statusLine.statusCode
		response.addHeader("X-Betamax", "REC")
		response.entity = proxyResponse.entity
	}

	private HttpRequest createProxyRequest(HttpRequest request) {
		def method = request.requestLine.method.toUpperCase(Locale.ENGLISH)
		switch (method) {
			case "DELETE":
				return new HttpDelete(request.requestLine.uri)
			case "GET":
				return new HttpGet(request.requestLine.uri)
			case "HEAD":
				return new HttpHead(request.requestLine.uri)
			case "OPTIONS":
				return new HttpOptions(request.requestLine.uri)
			default:
				throw new MethodNotSupportedException("$method method not supported")
		}
	}

	private HttpRequest createProxyRequest(HttpEntityEnclosingRequest request) {
		def method = request.requestLine.method.toUpperCase(Locale.ENGLISH)
		switch (method) {
			case "POST":
				def post = new HttpPost(request.requestLine.uri)
				post.entity = request.entity
				return post
			case "PUT":
				def put = new HttpPut(request.requestLine.uri)
				put.entity = request.entity
				return put
			default:
				throw new MethodNotSupportedException("$method method not supported")
		}
	}

}
