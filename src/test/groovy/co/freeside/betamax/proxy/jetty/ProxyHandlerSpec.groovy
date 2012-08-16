package co.freeside.betamax.proxy.jetty

import co.freeside.betamax.proxy.VetoingProxyInterceptor
import co.freeside.betamax.util.ProxyOverrider
import co.freeside.betamax.util.RequestCapturingMockHttpClient
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHttpResponse
import spock.lang.Specification
import co.freeside.betamax.util.servlet.*

import static java.net.HttpURLConnection.HTTP_PARTIAL
import static org.apache.http.HttpHeaders.*
import static org.apache.http.HttpVersion.HTTP_1_1

class ProxyHandlerSpec extends Specification {

    ProxyHandler handler = new ProxyHandler(false, new ProxyOverrider())
    MockHttpServletRequest request = new MockHttpServletRequest(method: "GET", requestURI: "/betamax")
    MockHttpServletResponse response = new MockHttpServletResponse()
    final BasicHttpResponse okResponse = new BasicHttpResponse(HTTP_1_1, 200, "OK")

    def "proceeds request if interceptor does not veto it"() {
        given:
        handler.httpClient = Mock(HttpClient)
        handler.interceptor = Mock(VetoingProxyInterceptor)

        when:
        handler.handle(null, null, request, response)

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
		handler.handle(null, null, request, response)

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
		handler.handle(null, null, request, response)

        then:
        response.status == upstreamResponse.statusLine.statusCode
        response.getHeader(CACHE_CONTROL) == upstreamResponse.getFirstHeader(CACHE_CONTROL).value
        response.body == upstreamResponse.entity.content.bytes
    }

    def "forwards request to target correctly"() {
        given:
        request.method = "POST"
        request.addHeader(IF_NONE_MATCH, "abc123")
        request.contentType = "application/x-www-form-urlencoded"
        request.characterEncoding = "UTF-8"
        request.body = "q=1".getBytes("UTF-8")

        and:
        def client = new RequestCapturingMockHttpClient()
        handler.httpClient = client

        when:
		handler.handle(null, null, request, response)

        then:
		client.lastRequest instanceof HttpPost
        client.lastRequest.getFirstHeader(IF_NONE_MATCH).value == "abc123"
        client.lastRequest.entity.content.text == "q=1"
    }
}