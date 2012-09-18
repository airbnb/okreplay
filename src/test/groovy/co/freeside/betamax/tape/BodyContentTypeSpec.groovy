package co.freeside.betamax.tape

import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@Issue('https://github.com/robfletcher/betamax/issues/53')
class BodyContentTypeSpec extends Specification {

	void 'identifies #mimeType as #type'() {
		expect:
		MemoryTape.isTextContentType(mimeType) ^ isBinary

		where:
		mimeType                 | isBinary
		'text/plain'             | false
		'application/json'       | false
		'application/javascript' | false
		'application/xml'        | false
		'application/rss+xml'    | false
		'application/atom+xml'   | false
		'application/rdf+xml'    | false
		'image/png'              | true

		type = isBinary ? 'binary' : 'text'
	}

}
