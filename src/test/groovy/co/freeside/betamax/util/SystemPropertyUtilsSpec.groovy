package co.freeside.betamax.util

import spock.lang.*
import co.freeside.betamax.util.SystemPropertyUtils

@Stepwise
class SystemPropertyUtilsSpec extends Specification {

	def cleanupSpec() {
		System.clearProperty("dummy.key")
	}

	def "can override a system property that does not exist"() {
		when:
		SystemPropertyUtils.override("dummy.key", "temporary value")

		then:
		System.properties."dummy.key" == "temporary value"
	}

	def "can reset so that the property is removed again"() {
		when:
		SystemPropertyUtils.resetAll()

		then:
		!System.properties.containsKey("dummy.key")
	}

	def "does not clear properties it has not overridden"() {
		given:
		System.properties."dummy.key" = "initial value"

		when:
		SystemPropertyUtils.resetAll()

		then:
		System.properties."dummy.key" == "initial value"
	}

	def "can override a system property that exists already"() {
		when:
		SystemPropertyUtils.override("dummy.key", "temporary value")

		then:
		System.properties."dummy.key" == "temporary value"
	}

	def "can reset back to the initial value"() {
		when:
		SystemPropertyUtils.resetAll()

		then:
		System.properties."dummy.key" == "initial value"
	}

}
