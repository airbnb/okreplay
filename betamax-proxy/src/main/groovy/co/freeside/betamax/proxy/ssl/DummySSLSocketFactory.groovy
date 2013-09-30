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

package co.freeside.betamax.proxy.ssl

import java.security.*
import javax.net.ssl.*
import org.apache.http.conn.ssl.SSLSocketFactory

class DummySSLSocketFactory extends SSLSocketFactory {

	private final SSLContext sslContext = SSLContext.getInstance('TLS')
	private final javax.net.ssl.SSLSocketFactory factory

	static DummySSLSocketFactory getInstance() {
		def trustStore = KeyStore.getInstance(KeyStore.defaultType)
		trustStore.load(null, null)
		new DummySSLSocketFactory(trustStore)
	}

	DummySSLSocketFactory(KeyStore trustStore) {
		super(trustStore)
		sslContext.init(null, [new DummyX509TrustManager()] as TrustManager[], new SecureRandom())
		factory = sslContext.socketFactory
		setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER)
	}

	@Override
	Socket createSocket(Socket socket, String host, int port, boolean autoClose) {
		factory.createSocket(socket, host, port, autoClose)
	}

	@Override
	Socket createSocket() {
		factory.createSocket()
	}
}