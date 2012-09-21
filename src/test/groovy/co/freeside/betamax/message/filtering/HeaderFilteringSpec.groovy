package co.freeside.betamax.message.filtering

import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.message.BasicResponse
import spock.lang.Specification
import spock.lang.Unroll

import static co.freeside.betamax.message.filtering.HeaderFilteringMessage.NO_PASS_HEADERS
import static org.apache.http.HttpHeaders.CONTENT_TYPE

@Unroll
class HeaderFilteringSpec extends Specification {

	void 'ServletRequestAdapter filters out #header headers'() {

		given: 'a servlet request'
		def request = new BasicRequest()
		request.addHeader(CONTENT_TYPE, 'text/plain')
		request.addHeader(header, 'value')

		when: 'we wrap it'
		def filteringRequest = new HeaderFilteringRequest(request)

		then: 'filtered headers are removed'
		filteringRequest.headers.keySet() == [CONTENT_TYPE] as Set
		filteringRequest.getHeader(header) == null

		and: 'unfiltered headers are still available'
		filteringRequest.getHeader(CONTENT_TYPE) == request.getHeader(CONTENT_TYPE)

		where:
		header << NO_PASS_HEADERS

	}

	void 'HttpResponseAdapter filters out #header headers'() {

		given: 'an HttpClient response'
		def response = new BasicResponse()
		response.addHeader(CONTENT_TYPE, 'text/plain')
		response.addHeader(header, 'value')

		when: 'we wrap it'
		def filteringResponse = new HeaderFilteringResponse(response)

		then: 'filtered headers are removed'
		filteringResponse.headers.keySet() == [CONTENT_TYPE] as Set
		filteringResponse.getHeader(header) == null

		and: 'unfiltered headers are still available'
		filteringResponse.getHeader(CONTENT_TYPE) == response.getHeader(CONTENT_TYPE)

		where:
		header << NO_PASS_HEADERS

	}

}
