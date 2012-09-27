package co.freeside.betamax

import grails.converters.JSON

class RestController {

	def index() {
		def response = withRest(uri: 'http://localhost:5000') {
			get requestContentType: 'application/json', path: '/'
		}
		println response.allHeaders.collect { "$it.name: $it.value" }
		render response.data as JSON
	}
}
