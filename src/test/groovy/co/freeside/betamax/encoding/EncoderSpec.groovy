package co.freeside.betamax.encoding

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class EncoderSpec extends Specification {

	void '#encoderClass can decode what it has encoded'() {
		when:
		def bytes = encoder.encode(text)

		then:
		new String(bytes) != text
		encoder.decode(new ByteArrayInputStream(bytes)) == text

		where:
		encoderClass << [GzipEncoder, DeflateEncoder]
		text = 'this is some text that gets encoded'
		encoder = encoderClass.newInstance()
	}

}
