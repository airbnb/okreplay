package co.freeside.betamax.proxy.ssl

import groovy.transform.InheritConstructors

import javax.net.ssl.*

@InheritConstructors
class DummyJVMSSLSocketFactory extends javax.net.ssl.SSLSocketFactory {
	SSLContext sslContext = SSLContext.getInstance("TLS")
	private javax.net.ssl.SSLSocketFactory factory
	@Override
	public Socket createSocket() throws IOException {
		return factory.createSocket()
	}

	@Override
	public Socket createSocket(InetAddress address, int port,
			InetAddress localAddress, int localPort) throws IOException {
		return factory.createSocket(address, port, localAddress, localPort)
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return factory.createSocket(host, port)
	}

	@Override
	public Socket createSocket(Socket s, String host, int port,
			boolean autoClose) throws IOException {
		return factory.createSocket(s, host, port, autoClose)
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost,
			int localPort) throws IOException, UnknownHostException {
		return factory.createSocket(host, port, localHost, localPort)
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		return factory.createSocket(host, port)
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return factory.getDefaultCipherSuites()
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return factory.getSupportedCipherSuites()
	}

	public DummyJVMSSLSocketFactory() {
		sslContext.init(null, [new DummyX509TrustManager()] as TrustManager[], new java.security.SecureRandom())
		factory = sslContext.socketFactory
	}
	
	public static javax.net.ssl.SSLSocketFactory getDefault() {
		return new DummyJVMSSLSocketFactory()
	}


}