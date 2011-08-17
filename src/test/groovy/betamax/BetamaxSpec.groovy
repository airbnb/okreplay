package betamax

import spock.lang.Specification

class BetamaxSpec extends Specification {

	def "exception thrown when no tape loaded"() {
		when:
		Betamax.instance.tape

		then:
		thrown(IllegalStateException)
	}
	
}
