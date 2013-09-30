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

import javax.net.ssl.*
import groovy.transform.InheritConstructors

@InheritConstructors
class DummyJVMSSLSocketFactory extends SSLSocketFactory {

	private final SSLContext sslContext = SSLContext.getInstance('TLS')
	private final SSLSocketFactory factory

	@Override
	Socket createSocket() {
		factory.createSocket()
	}

	@Override
	Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) {
		factory.createSocket(address, port, localAddress, localPort)
	}

	@Override
	Socket createSocket(InetAddress host, int port) {
		factory.createSocket(host, port)
	}

	@Override
	Socket createSocket(Socket s, String host, int port, boolean autoClose) {
		factory.createSocket(s, host, port, autoClose)
	}

	@Override
	Socket createSocket(String host, int port, InetAddress localHost, int localPort) {
		factory.createSocket(host, port, localHost, localPort)
	}

	@Override
	Socket createSocket(String host, int port) {
		factory.createSocket(host, port)
	}

	@Override
	String[] getDefaultCipherSuites() {
		factory.defaultCipherSuites
	}

	@Override
	String[] getSupportedCipherSuites() {
		factory.supportedCipherSuites
	}

	DummyJVMSSLSocketFactory() {
		sslContext.init(null, [new DummyX509TrustManager()] as TrustManager[], new java.security.SecureRandom())
		factory = sslContext.socketFactory
	}


}