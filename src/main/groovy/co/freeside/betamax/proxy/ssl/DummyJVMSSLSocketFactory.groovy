package co.freeside.betamax.proxy.ssl

import groovy.transform.InheritConstructors

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

@InheritConstructors
class DummyJVMSSLSocketFactory extends javax.net.ssl.SSLSocketFactory {

	SSLContext sslContext = SSLContext.getInstance('TLS')
	private javax.net.ssl.SSLSocketFactory factory

	@Override
	Socket createSocket() throws IOException {
		factory.createSocket()
	}

	@Override
	Socket createSocket(InetAddress address, int port,
			InetAddress localAddress, int localPort) throws IOException {
		factory.createSocket(address, port, localAddress, localPort)
	}

	@Override
	Socket createSocket(InetAddress host, int port) throws IOException {
		factory.createSocket(host, port)
	}

	@Override
	Socket createSocket(Socket s, String host, int port,
			boolean autoClose) throws IOException {
		factory.createSocket(s, host, port, autoClose)
	}

	@Override
	Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
		factory.createSocket(host, port, localHost, localPort)
	}

	@Override
	Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
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
	
	static javax.net.ssl.SSLSocketFactory getDefault() {
		new DummyJVMSSLSocketFactory()
	}


}