package betamax.encoding

import spock.lang.Specification

class GzipEncoderSpec extends Specification {

	def "can decode what it has encoded"() {
		given:
		def bytes = GzipEncoder.encode("this is some text that gets gzipped")

		expect:
		new String(bytes) != "this is some text that gets gzipped"
		GzipEncoder.decode(new ByteArrayInputStream(bytes)) == "this is some text that gets gzipped"
	}
	
}
