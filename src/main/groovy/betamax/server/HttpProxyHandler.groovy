package betamax.server

import betamax.Betamax
import groovy.util.logging.Log4j
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import org.apache.http.client.methods.*
import org.apache.http.protocol.*

@Log4j
class HttpProxyHandler implements HttpRequestHandler {

	private static final X_BETAMAX = "X-Betamax"
	private static final PROXY_CONNECTION = "Proxy-Connection"
	private static final KEEP_ALIVE = "Keep-Alive"
	private static final NO_PASS_HEADERS = [
			CONTENT_LENGTH,
			HOST,
			PROXY_CONNECTION,
			CONNECTION,
			KEEP_ALIVE,
			PROXY_AUTHENTICATE,
			PROXY_AUTHORIZATION,
			TE,
			TRAILER,
			TRANSFER_ENCODING,
			UPGRADE
	].toSet().asImmutable()

	private final HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager())

	void handle(HttpRequest request, HttpResponse response, HttpContext context) {
		log.debug "proxying request $request.requestLine..."

		def tape = Betamax.instance.tape

		if (tape?.play(request, response)) {
			log.debug "playing back from tape '$tape.name'..."
			response.addHeader(X_BETAMAX, "PLAY")
		} else {
			execute(request, response)
			response.addHeader(VIA, "Betamax")
			if (tape) {
				log.debug "recording response with status $response.statusLine to tape '$tape.name'..."
				tape.record(request, response)
				response.addHeader(X_BETAMAX, "REC")
			} else {
				log.debug "no tape inserted..."
			}
		}
	}

	private void execute(HttpRequest request, HttpResponse response) {
		def proxyRequest = createProxyRequest(request)
		copyRequestData(request, proxyRequest)
		proxyRequest.addHeader(VIA, "Betamax")

		def proxyResponse = httpClient.execute(proxyRequest)

		copyResponseData(proxyResponse, response)
	}

	private void copyRequestData(HttpRequest from, HttpRequest to) {
		for (header in from.allHeaders) {
			if (!(header.name in NO_PASS_HEADERS)) {
				to.addHeader(header)
			}
		}

		if (from instanceof HttpEntityEnclosingRequest) {
			to.entity = from.entity
		}
	}

	private void copyResponseData(HttpResponse from, HttpResponse to) {
		to.statusCode = from.statusLine.statusCode
		for (header in from.allHeaders) {
			if (!(header.name in NO_PASS_HEADERS)) {
				to.addHeader(header)
			}
		}
		to.entity = from.entity
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
			case "POST":
				return new HttpPost(request.requestLine.uri)
			case "PUT":
				return new HttpPut(request.requestLine.uri)
			default:
				throw new MethodNotSupportedException("$method method not supported")
		}
	}

}
