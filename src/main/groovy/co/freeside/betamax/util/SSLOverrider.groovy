package co.freeside.betamax.util

import java.security.Security
import javax.net.ssl.HttpsURLConnection
import co.freeside.betamax.ssl.DummyJVMSSLSocketFactory
import static org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER

class SSLOverrider {

	public static final String SSL_SOCKET_FACTORY_PROVIDER = 'ssl.SocketFactory.provider'

	private boolean isActive = false
	private originalSocketFactoryProvider
	private originalHostnameVerifier

	void activate() {
		if (!isActive) {
			originalSocketFactoryProvider = Security.getProperty(SSL_SOCKET_FACTORY_PROVIDER)
			originalHostnameVerifier = HttpsURLConnection.defaultHostnameVerifier

			Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, DummyJVMSSLSocketFactory.name)

			HttpsURLConnection.defaultHostnameVerifier = ALLOW_ALL_HOSTNAME_VERIFIER
		}

		isActive = true
	}

	void deactivate() {
		if (isActive) {
			Security.setProperty(SSL_SOCKET_FACTORY_PROVIDER, originalSocketFactoryProvider ?: '')
			HttpsURLConnection.defaultHostnameVerifier = originalHostnameVerifier
		}

		isActive = false
	}

}
