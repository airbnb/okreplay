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

package co.freeside.betamax.proxy.netty

import co.freeside.betamax.*
import co.freeside.betamax.handler.DefaultHandlerChain
import co.freeside.betamax.util.*
import org.apache.http.client.HttpClient
import org.apache.http.conn.scheme.Scheme
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.*
import org.apache.http.params.HttpConnectionParams

class ProxyServer implements HttpInterceptor {

	private final NettyBetamaxServer proxyServer;
	private final BetamaxChannelHandler proxyHandler;
	private final ProxyRecorder recorder
	private final ProxyOverrider proxyOverrider = new ProxyOverrider()
	private final SSLOverrider sslOverrider = new SSLOverrider()

	ProxyServer(ProxyRecorder recorder) {
		this.recorder = recorder

		proxyHandler = new BetamaxChannelHandler()
		proxyHandler << new DefaultHandlerChain(recorder, newHttpClient())

		def channel = new HttpChannelInitializer(0, proxyHandler); // TODO: correct worker threads? After all nothing in Betamax is actually async so we should probably not tie up the main thread
		proxyServer = new NettyBetamaxServer(recorder.proxyPort, channel)
	}

	@Override
	boolean isRunning() {
		proxyHandler.active
	}

	void start() {
//		def connectHandler = new CustomConnectHandler(handler, port + 1)

		proxyServer.run()

		overrideProxySettings()
		overrideSSLSettings()
	}

	@Override
	void stop() {
		restoreOriginalProxySettings()
		restoreOriginalSSLSettings()

		proxyServer.shutdown()
	}

	@Override
	String getHost() {
		recorder.proxyHost // TODO: this should come from the Netty server instance
	}

	@Override
	int getPort() {
		recorder.proxyPort
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
		def proxyHost = InetAddress.localHost.hostAddress
		def nonProxyHosts = recorder.ignoreHosts as Set
		if (recorder.ignoreLocalhost) {
			nonProxyHosts.addAll(Network.localAddresses)
		}
		proxyOverrider.activate proxyHost, port, nonProxyHosts
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

