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

package co.freeside.betamax.encoding

import spock.lang.*

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
