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
import co.freeside.betamax.proxy.RecordAndPlaybackProxyInterceptor
import org.eclipse.jetty.server.handler.ConnectHandler
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector

import java.nio.channels.SocketChannel
import javax.servlet.http.HttpServletRequest

import org.eclipse.jetty.server.*

import static co.freeside.betamax.Recorder.DEFAULT_PROXY_PORT

class ProxyServer extends SimpleServer {

	ProxyServer() {
		super(DEFAULT_PROXY_PORT)
	}

	void start(Recorder recorder) {
		def handler = new ProxyHandler(recorder.sslSupport)
		handler.interceptor = new RecordAndPlaybackProxyInterceptor(recorder)
		handler.timeout = recorder.proxyTimeout

		def connectHandler = new CustomConnectHandler(handler, port+1)

		super.start(connectHandler)
	}

	@Override
	protected Server createServer(int port) {
		def server = super.createServer(port)
		server.addConnector(createSSLConnector(port + 1))
		server
	}

	private Connector createSSLConnector(int port) {
		def sslConnector = new SslSelectChannelConnector()
		sslConnector.port = port // TODO: separate property
		sslConnector.keystore = new File("src/main/resources/keystore").absolutePath // TODO: need to make this a classpath resource
		sslConnector.password = "password"
		sslConnector.keyPassword = "password"
		return sslConnector
	}
}

class CustomConnectHandler extends ConnectHandler {
	int sslPort

	public CustomConnectHandler(Handler handler, int sslPort) {
		super(handler)
		this.sslPort=sslPort
	}

	@Override
	protected SocketChannel connect(HttpServletRequest request, String host, int port) throws IOException {
		SocketChannel channel = SocketChannel.open()
		channel.socket().setTcpNoDelay(true)
		channel.socket().connect(new InetSocketAddress("127.0.0.1", sslPort), getConnectTimeout())
		return channel
	}
}