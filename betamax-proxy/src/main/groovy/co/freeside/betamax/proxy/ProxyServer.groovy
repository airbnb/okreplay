/*
 * Copyright 2013 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.proxy

import co.freeside.betamax.*
import co.freeside.betamax.handler.DefaultHandlerChain
import co.freeside.betamax.util.*
import org.apache.http.client.HttpClient
import org.apache.http.conn.scheme.Scheme
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.*
import org.apache.http.params.HttpConnectionParams
import org.littleshoot.proxy.*
import org.littleshoot.proxy.impl.*

class ProxyServer implements HttpInterceptor {

	private final HttpProxyServerBootstrap proxyServerBootstrap;
	private final ProxyRecorder recorder
	private final ProxyOverrider proxyOverrider = new ProxyOverrider()
	private final SSLOverrider sslOverrider = new SSLOverrider()
	private HttpProxyServer proxyServer;
	private boolean running = false
	private final InetSocketAddress address

	ProxyServer(ProxyRecorder recorder) {
		this.recorder = recorder

		def handlerChain = new DefaultHandlerChain(recorder, newHttpClient())
		address = new InetSocketAddress(NetworkUtils.getLocalHost(), recorder.getProxyPort());
		println "created address, $address"
		proxyServerBootstrap = DefaultHttpProxyServer
				.bootstrap()
				.withAddress(address)
				.withFiltersSource(new BetamaxFiltersSource())
	}

	@Override
	boolean isRunning() {
		running
	}

	void start() {
		if (isRunning()) throw new IllegalStateException("Betamax proxy server is already running")
		proxyServer = proxyServerBootstrap.start()
		running = true

		overrideProxySettings()
		overrideSSLSettings()
	}

	@Override
	void stop() {
		if (!isRunning()) throw new IllegalStateException("Betamax proxy server is already stopped")
		restoreOriginalProxySettings()
		restoreOriginalSSLSettings()

		proxyServer.stop()
		running = false
	}

	@Override
	String getHost() {
		address.hostName
	}

	@Override
	int getPort() {
		address.port
	}

	private HttpClient newHttpClient() {
		def connectionManager = new PoolingClientConnectionManager()
		def httpClient = new DefaultHttpClient(connectionManager)
		httpClient.routePlanner = new ProxySelectorRoutePlanner(
				httpClient.connectionManager.schemeRegistry,
				proxyOverrider.originalProxySelector
		)
		if (recorder.sslSupport) {
			connectionManager.schemeRegistry.register new Scheme('https', recorder.sslSocketFactory, 443)
		}
		HttpConnectionParams.setConnectionTimeout(httpClient.params, recorder.proxyTimeout)
		HttpConnectionParams.setSoTimeout(httpClient.params, recorder.proxyTimeout)
		httpClient
	}

	private void overrideProxySettings() {
		def nonProxyHosts = recorder.ignoreHosts as Set
		if (recorder.ignoreLocalhost) {
			nonProxyHosts.addAll(Network.localAddresses)
		}
		proxyOverrider.activate address.hostName, address.port, nonProxyHosts
	}

	private void restoreOriginalProxySettings() {
		proxyOverrider.deactivateAll()
	}

	private void overrideSSLSettings() {
		if (recorder.sslSupport) {
			sslOverrider.activate()
		}
	}

	private void restoreOriginalSSLSettings() {
		if (recorder.sslSupport) {
			sslOverrider.deactivate()
		}
	}

}

