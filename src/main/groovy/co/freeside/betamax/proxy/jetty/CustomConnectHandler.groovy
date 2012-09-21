package co.freeside.betamax.proxy.jetty

import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.handler.ConnectHandler

import javax.servlet.http.HttpServletRequest
import java.nio.channels.SocketChannel

class CustomConnectHandler extends ConnectHandler {

	int sslPort

	CustomConnectHandler(Handler handler, int sslPort) {
		super(handler)
		this.sslPort = sslPort
	}

	@Override
	protected SocketChannel connect(HttpServletRequest request, String host, int port) {
		def channel = SocketChannel.open()
		channel.socket().tcpNoDelay = true
		channel.socket().connect(new InetSocketAddress('127.0.0.1', sslPort), connectTimeout)
		channel
	}
}
