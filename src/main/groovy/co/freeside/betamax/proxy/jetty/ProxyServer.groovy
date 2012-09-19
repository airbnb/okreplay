/*
 * Copyright 2011 Rob Fletcher
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

package co.freeside.betamax.proxy.jetty

import co.freeside.betamax.Recorder
import co.freeside.betamax.proxy.handler.HeaderFilter
import co.freeside.betamax.proxy.handler.TapeReader
import co.freeside.betamax.proxy.handler.TapeWriter
import co.freeside.betamax.proxy.handler.TargetConnector
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import org.apache.http.client.HttpClient
import org.apache.http.conn.scheme.Scheme
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.HttpConnectionParams
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector

import static co.freeside.betamax.Recorder.DEFAULT_PROXY_PORT

class ProxyServer extends SimpleServer {

	ProxyServer() {
		super(DEFAULT_PROXY_PORT)
	}

	void start(Recorder recorder) {
		def handler = new BetamaxProxy()
		handler <<
				new TapeReader(recorder) <<
				new TapeWriter(recorder) <<
				new HeaderFilter() <<
				new TargetConnector(newHttpClient(recorder))

		def connectHandler = new CustomConnectHandler(handler, port + 1)

		super.start(connectHandler)
	}

	private HttpClient newHttpClient(Recorder recorder) {
		def connectionManager = new ThreadSafeClientConnManager()
		def httpClient = new DefaultHttpClient(connectionManager)
		httpClient.routePlanner = new ProxySelectorRoutePlanner(
				httpClient.connectionManager.schemeRegistry,
				recorder.proxyOverrider.originalProxySelector
		)
		if (recorder.sslSupport) {
			connectionManager.schemeRegistry.register new Scheme('https', DummySSLSocketFactory.instance, 443)
		}
		HttpConnectionParams.setConnectionTimeout(httpClient.params, recorder.proxyTimeout)
		HttpConnectionParams.setSoTimeout(httpClient.params, recorder.proxyTimeout)
		httpClient
	}

	@Override
	protected Server createServer(int port) {
		def server = super.createServer(port)
		server.addConnector(createSSLConnector(port + 1))
		server
	}

	private Connector createSSLConnector(int port) {
		new SslSelectChannelConnector(
				port: port, // TODO: separate property
				keystore: Class.getResource('/keystore'),
				password: 'password',
				keyPassword: 'password'
		)
	}
}

