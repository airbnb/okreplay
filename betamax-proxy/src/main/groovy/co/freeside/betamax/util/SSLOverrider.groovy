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

import java.security.Security
import javax.net.ssl.HttpsURLConnection
import co.freeside.betamax.proxy.ssl.DummyJVMSSLSocketFactory
import static org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER

class SSLOverrider {

	public static final String SSL_SOCKET_FACTORY_PROVIDER = 'ssl.SocketFactory.provider'

	private boolean isActive = false
	private originalSocketFactoryProvider
	private originalHostnameVerifier

	void activate() {
		if (!isActive) {
			originalSocketFactoryProvider = Security.getProperty(SSL_SOCKET_FACTORY_PROVIDER)
			originalHostnameVerifier = HttpsURLConnection.defaultHostnameVerifier

			Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, DummyJVMSSLSocketFactory.name)

			HttpsURLConnection.defaultHostnameVerifier = ALLOW_ALL_HOSTNAME_VERIFIER
		}

		isActive = true
	}

	void deactivate() {
		if (isActive) {
			Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, originalSocketFactoryProvider ?: '')
			HttpsURLConnection.defaultHostnameVerifier = originalHostnameVerifier
		}

		isActive = false
	}

}
