package co.freeside.betamax.ssl

import java.security.*
import javax.net.ssl.*
import org.apache.http.conn.ssl.SSLSocketFactory

class DummySSLSocketFactory extends SSLSocketFactory {

	SSLContext sslContext = SSLContext.getInstance('TLS')

	private javax.net.ssl.SSLSocketFactory factory

	static DummySSLSocketFactory getInstance() {
		def trustStore = KeyStore.getInstance(KeyStore.defaultType)
		trustStore.load(null, null)
		new DummySSLSocketFactory(trustStore)
	}

	DummySSLSocketFactory(KeyStore trustStore) {
		super(trustStore)
		sslContext.init(null, [new DummyX509TrustManager()] as TrustManager[], new SecureRandom())
		factory = sslContext.socketFactory
		setHostnameVerifier(new DummyHostNameVerifier())
	}

	@Override
	Socket createSocket(Socket socket, String host, int port, boolean autoClose) {
		factory.createSocket(socket, host, port, autoClose)
	}

	@Override
	Socket createSocket() {
		factory.createSocket()
	}
}