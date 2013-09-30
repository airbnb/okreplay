/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
