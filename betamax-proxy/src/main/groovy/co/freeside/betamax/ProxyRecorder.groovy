/*
 * Copyright 2012 the original author or authors.
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

package co.freeside.betamax

import co.freeside.betamax.proxy.ProxyServer
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import co.freeside.betamax.util.PropertiesCategory
import groovy.transform.InheritConstructors
import org.apache.http.conn.ssl.SSLSocketFactory

@InheritConstructors
class ProxyRecorder extends Recorder {

	public static final int DEFAULT_PROXY_PORT = 5555
	public static final int DEFAULT_PROXY_TIMEOUT = 5000
	public static final SSLSocketFactory DEFAULT_SSL_SOCKET_FACTORY = DummySSLSocketFactory.instance

	/**
	 * The port the Betamax proxy will listen on.
	 */
	int proxyPort

	/**
	 * The time (in milliseconds) the proxy will wait before aborting a request.
	 */
	int proxyTimeout

	/**
	 * If set to true add support for proxying SSL (disable certificate checking).
	 */
	boolean sslSupport

	/**
	 * The factory that will be used to create SSL sockets for secure connections to the target.
	 */
	SSLSocketFactory sslSocketFactory

	private ProxyServer interceptor

	/**
	 * @return the hostname or address where the proxy will run.
	 */
	String getProxyHost() {
		interceptor.host
	}

	/**
	 * @return a `java.net.Proxy` instance configured to point to the Betamax proxy.
	 */
	Proxy getProxy() {
		new Proxy(Proxy.Type.HTTP, new InetSocketAddress(interceptor.host, interceptor.port))
	}

	@Override
	void start(String tapeName, Map arguments) {
		if (!interceptor) {
			interceptor = new ProxyServer(this)
		}
		if (!interceptor.running) {
			interceptor.start()
		}
		super.start(tapeName, arguments)
	}

	@Override
	void stop() {
		interceptor.stop()
		super.stop()
	}

	@Override
	protected void configureFrom(Properties properties) {
		super.configureFrom(properties)

		use(PropertiesCategory) {
			proxyPort = properties.getInteger('betamax.proxyPort', DEFAULT_PROXY_PORT)
			proxyTimeout = properties.getInteger('betamax.proxyTimeout', DEFAULT_PROXY_TIMEOUT)
			sslSupport = properties.getBoolean('betamax.sslSupport')
		}
	}

	@Override
	protected void configureFrom(ConfigObject config) {
		super.configureFrom(config)

		proxyPort = config.betamax.proxyPort ?: DEFAULT_PROXY_PORT
		proxyTimeout = config.betamax.proxyTimeout ?: DEFAULT_PROXY_TIMEOUT
		sslSupport = config.betamax.sslSupport
		sslSocketFactory = config.betamax.sslSocketFactory ?: DEFAULT_SSL_SOCKET_FACTORY
	}

	@Override
	protected void configureWithDefaults() {
		super.configureWithDefaults()

		proxyPort = DEFAULT_PROXY_PORT
		proxyTimeout = DEFAULT_PROXY_TIMEOUT
		sslSupport = false
		sslSocketFactory = DEFAULT_SSL_SOCKET_FACTORY
	}
}
