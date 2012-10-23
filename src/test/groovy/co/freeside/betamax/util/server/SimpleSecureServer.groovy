package co.freeside.betamax.util.server

import co.freeside.betamax.proxy.jetty.SimpleServer
import groovy.transform.InheritConstructors
import org.eclipse.jetty.server.*
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector

@InheritConstructors
class SimpleSecureServer extends SimpleServer {

	@Override
	String getUrl() {
		"https://$host:$port/"
	}

	@Override
	protected Server createServer(int port) {
		def server = super.createServer(port)

		def connector = new SslSelectChannelConnector()

		def keystore = SimpleSecureServer.getResource('/betamax.keystore')

		connector.port = port
		connector.keystore = keystore
		connector.password = 'password'
		connector.keyPassword = 'password'

		server.connectors = [connector]as Connector[]

		server
	}
}
