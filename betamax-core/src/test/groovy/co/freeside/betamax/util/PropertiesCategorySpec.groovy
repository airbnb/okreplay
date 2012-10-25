package co.freeside.betamax.util

import spock.lang.*
import static co.freeside.betamax.TapeMode.*

@Unroll
class PropertiesCategorySpec extends Specification {

	void setupSpec() {
		Properties.mixin(PropertiesCategory)
	}

	void 'can get boolean properties'() {
		given:
		def properties = new Properties()
		if (value) {
			properties.setProperty('key', value)
		}

		expect:
		properties.getBoolean('key', defaultValue) == result

		where:
		value   | defaultValue | result
		'true'  | false        | true
		'false' | true         | false
		null    | false        | false
		null    | true         | true
	}

	void 'can get integer properties'() {
		given:
		def properties = new Properties()
		if (value != null) {
			properties.setProperty('key', value)
		}

		expect:
		properties.getInteger('key', defaultValue) == result

		where:
		value | defaultValue | result
		'1'   | -1           | 1
		'0'   | -1           | 0
		null  | -1           | -1
	}

	void 'can get enum properties'() {
		given:
		def properties = new Properties()
		if (value != null) {
			properties.setProperty('key', value)
		}

		expect:
		properties.getEnum('key', defaultValue) == result

		where:
		value            | defaultValue | result
		READ_ONLY.name() | READ_WRITE   | READ_ONLY
		null             | READ_WRITE   | READ_WRITE
	}

}
