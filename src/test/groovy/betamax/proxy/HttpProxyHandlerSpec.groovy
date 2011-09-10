package betamax.proxy

import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHttpResponse

import spock.lang.Specification
import static java.net.HttpURLConnection.HTTP_PARTIAL
import org.apache.http.*
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.client.*
import org.apache.http.client.methods.*

import betamax.util.RequestCapturingMockHttpClient
import betamax.proxy.httpcore.HttpProxyHandler

class HttpProxyHandlerSpec extends Specification {

	HttpProxyHandler handler = new HttpProxyHandler()
	HttpRequest request = new HttpGet("http://robfletcher.github.com/betamax")
	HttpResponse response = new BasicHttpResponse(HTTP_1_1, 200, "OK")
	final BasicHttpResponse okResponse = new BasicHttpResponse(HTTP_1_1, 200, "OK")

	def "proceeds request if interceptor does not veto it"() {
		given:
		handler.httpClient = Mock(HttpClient)
		handler.interceptor = Mock(VetoingProxyInterceptor)

		when:
		handler.handle(request, response, null)

		then:
		1 * handler.interceptor.interceptRequest(_, _) >> false
		1 * handler.httpClient.execute(_) >> okResponse
		1 * handler.interceptor.interceptResponse(_, _)
	}

	def "does not proceed request if interceptor vetoes it"() {
		given:
		handler.httpClient = Mock(HttpClient)
		handler.interceptor = Mock(VetoingProxyInterceptor)

		when:
		handler.handle(request, response, null)

		then:
		1 * handler.interceptor.interceptRequest(_, _) >> true
		0 * handler.httpClient.execute(_)
		0 * handler.interceptor.interceptResponse(_, _)
	}

	def "copies proxy response to actual response"() {
		given:
		def upstreamResponse = new BasicHttpResponse(HTTP_1_1, HTTP_PARTIAL, "Partial")
		upstreamResponse.addHeader(CACHE_CONTROL, "private, max-age=3600")
		upstreamResponse.entity = new StringEntity("O HAI", "text/plain", "UTF-8")

		and:
		handler.httpClient = Mock(HttpClient)
		handler.httpClient.execute(_) >> upstreamResponse

		when:
		handler.handle(request, response, null)

		then:
		response.statusLine.statusCode == upstreamResponse.statusLine.statusCode
		response.getFirstHeader(CACHE_CONTROL).value == upstreamResponse.getFirstHeader(CACHE_CONTROL).value
		response.entity.content.text == upstreamResponse.entity.content.text
	}

	def "forwards request to target correctly"() {
		given:
		def postRequest = new HttpPost("http://robfletcher.github.com/betamax")
		postRequest.addHeader(IF_NONE_MATCH, "abc123")
		postRequest.entity = new StringEntity("q=1", "application/x-www-form-urlencoded", "UTF-8")

		and:
		def client = new RequestCapturingMockHttpClient()
		handler.httpClient = client

		when:
		handler.handle(postRequest, response, null)

		then:
		client.lastRequest.getFirstHeader(IF_NONE_MATCH).value == postRequest.getFirstHeader(IF_NONE_MATCH).value
		client.lastRequest.entity.content.text == postRequest.entity.content.text
	}

}

