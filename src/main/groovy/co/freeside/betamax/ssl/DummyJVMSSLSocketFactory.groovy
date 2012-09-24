package co.freeside.betamax.ssl

import javax.net.ssl.*
import groovy.transform.InheritConstructors

@InheritConstructors
class DummyJVMSSLSocketFactory extends SSLSocketFactory {

	SSLContext sslContext = SSLContext.getInstance('TLS')
	private javax.net.ssl.SSLSocketFactory factory

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