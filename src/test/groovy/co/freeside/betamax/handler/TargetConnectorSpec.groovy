package co.freeside.betamax.handler

import co.freeside.betamax.handler.*
import co.freeside.betamax.util.message.BasicRequest
import org.apache.http.*
import org.apache.http.client.HttpClient
import org.apache.http.message.BasicHttpResponse
import spock.lang.*

@Unroll
class TargetConnectorSpec extends Specification {

	HttpClient httpClient = Mock(HttpClient)
	TargetConnector handler = new TargetConnector(httpClient)

	BasicRequest request = new BasicRequest('GET', 'http://freeside.co/')
	HttpResponse okResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, 'OK')

	void 'proceeds request to original target and returns response'() {
		when:
		def response = handler.handle(request)

		then:
		1 * httpClient.execute(_, _) >> { httpHost, outboundRequest ->
			assert outboundRequest.requestLine.method == request.method
			assert outboundRequest.requestLine.uri == request.uri.toString()
			okResponse
		}

		and:
		response.status == 200
	}

	void 'uses #method for the outbound request method'() {
		given:
		request.method = method

		when:
		handler.handle(request)

		then:
		1 * httpClient.execute(_, _) >> { httpHost, outboundRequest ->
			assert outboundRequest.requestLine.method == request.method.toString()
			okResponse
		}

		where:
		method << ['GET', 'HEAD', 'POST', 'PUT', 'DELETE', 'OPTIONS']
	}

	void 'sends headers with outbound request'() {
		given:
		request.addHeader('Accept-Language', 'en_GB, en')
		request.addHeader('If-None-Match', 'abc123')

		when:
		handler.handle(request)

		then:
		1 * httpClient.execute(_, _) >> { httpHost, outboundRequest ->
			assert request.headers.every { name, value ->
				outboundRequest.getFirstHeader(name)?.value == value
			}
			okResponse
		}
	}

	void 'sends body with outbound request'() {
		given:
		request.method = 'POST'
		request.addHeader('Content-Type', 'application/x-www-form-urlencoded')
		request.body = 'price=\u003199.99'.getBytes('ISO-8859-1')

		when:
		handler.handle(request)

		then:
		1 * httpClient.execute(_, _) >> { httpHost, outboundRequest ->
			def entity = outboundRequest.entity
			assert entity.content.text == request.bodyAsText.text
			okResponse
		}
	}

	void 'throws an exception with HTTP status #httpStatus if outbound request #description'() {
		given:
		httpClient.execute(_, _) >> { throw originalExceptionClass.newInstance() }

		when:
		handler.handle(request)

		then:
		thrown expectedExceptionClass

		where:
		originalExceptionClass | expectedExceptionClass | description
		SocketTimeoutException | TargetTimeoutException | 'times out'
		IOException            | TargetErrorException   | 'fails'
	}

}
