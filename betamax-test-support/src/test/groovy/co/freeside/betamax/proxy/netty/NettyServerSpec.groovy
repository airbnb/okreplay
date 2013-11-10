/*
 * Copyright 2013 the original author or authors.
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

package co.freeside.betamax.proxy.netty

import co.freeside.betamax.util.server.*
import com.google.common.base.Charsets
import spock.lang.Specification
import static com.google.common.base.Charsets.UTF_8

class NettyServerSpec extends Specification {

	void "can serve HTTP responses with Netty"() {
		given:
		def server = new SimpleServer(port, EchoHandler)
		server.start()

		when:
		HttpURLConnection connection = new URL("http://localhost:$port/").openConnection()
		connection.requestMethod = "POST"
		connection.doInput = true
		connection.doOutput = true
		connection.outputStream.withWriter(UTF_8.toString()) { writer ->
			writer << message
		}
		connection.connect()

		then:
		connection.inputStream.getText(UTF_8.toString()).endsWith(message)

		cleanup:
		server.stop()

		where:
		port = 5000
		message = "O HAI"
	}

}