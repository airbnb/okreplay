package betamax.server

import betamax.Recorder
import groovy.util.logging.Log4j
import org.apache.http.client.HttpClient
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import org.apache.http.client.methods.*
import org.apache.http.protocol.*
import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY

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

		def tape = Recorder.instance.tape

		if (tape?.play(request, response)) {
			log.debug "playing back from tape '$tape.name'..."
			response.addHeader(X_BETAMAX, "PLAY")
		} else {
			try {
				execute(request, response)
				response.addHeader(VIA, "Betamax")
				if (tape) {
					log.debug "recording response with status $response.statusLine to tape '$tape.name'..."
					tape.record(request, response)
					response.addHeader(X_BETAMAX, "REC")
				} else {
					log.debug "no tape inserted..."
				}
			} catch (IOException e) {
				log.error "problem connecting to $request.requestLine.uri: $e.message"
				response.statusCode = HTTP_BAD_GATEWAY
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
			to.entity = copyEntity(from.entity)
		}
	}

	private void copyResponseData(HttpResponse from, HttpResponse to) {
		to.statusCode = from.statusLine.statusCode
		for (header in from.allHeaders) {
			if (!(header.name in NO_PASS_HEADERS)) {
				to.addHeader(header)
			}
		}
		if (from.entity) {
			to.entity = copyEntity(from.entity)
		}
	}

	private HttpEntity copyEntity(HttpEntity entity) {
		if (entity.isRepeatable()) {
			log.debug "re-using repeatable entity with content type ${entity.contentType?.value}..."
			entity
		} else {
			log.debug "copying non-repeatable entity ${entity.getClass().name} with content type ${entity.contentType?.value}..."
			def bytes = new ByteArrayOutputStream()
			entity.writeTo(bytes)
			def copy = new ByteArrayEntity(bytes.toByteArray())
			copy.chunked = entity.chunked
			copy.contentEncoding = entity.contentEncoding
			copy.contentType = entity.contentType
			log.debug "copied entity..."
			copy
		}
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
