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

package software.betamax.util

import software.betamax.proxy.ssl.DummyJVMSSLSocketFactory
import spock.lang.Specification

import java.security.Security

import static software.betamax.util.SSLOverrider.SSL_SOCKET_FACTORY_PROVIDER

class SSLOverriderSpec extends Specification {

	SSLOverrider sslOverrider = new SSLOverrider()

	void 'overrides SSL settings when activated'() {
		when:
		sslOverrider.activate()

		then:
		Security.getProperty(SSL_SOCKET_FACTORY_PROVIDER) ==  DummyJVMSSLSocketFactory.name

		cleanup:
		sslOverrider.deactivate()
	}

	void 'restores original SSL settings when deactivated'() {
		when:
		sslOverrider.activate()
		sslOverrider.deactivate()

		then:
		Security.getProperty(SSL_SOCKET_FACTORY_PROVIDER) ==  old(Security.getProperty(SSL_SOCKET_FACTORY_PROVIDER))
	}

}
