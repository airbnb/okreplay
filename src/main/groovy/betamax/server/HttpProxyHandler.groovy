package betamax.server

import org.apache.http.protocol.HttpRequestHandler
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.protocol.HttpContext
import org.apache.http.MethodNotSupportedException
import org.apache.http.client.methods.HttpGet

class HttpProxyHandler implements HttpRequestHandler {

    private final HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager())

    void handle(HttpRequest request, HttpResponse response, HttpContext context) {
        def method = request.requestLine.method.toUpperCase(Locale.ENGLISH)
        if (method != "GET") {
            throw new MethodNotSupportedException("$method method not supported")
        }

        println "${Thread.currentThread().name}:: request for $request.requestLine.uri"

        def proxyRequest = new HttpGet(request.requestLine.uri)
        def proxyResponse = httpClient.execute(proxyRequest)

        println "${Thread.currentThread().name}:: serving response with status $proxyResponse.statusLine.statusCode"

        response.statusCode = proxyResponse.statusLine.statusCode
        response.addHeader("X-Betamax", "REC")
        response.entity = proxyResponse.entity
    }

}
