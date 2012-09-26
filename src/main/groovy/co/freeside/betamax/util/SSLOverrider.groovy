package co.freeside.betamax.util

import java.security.Security
import javax.net.ssl.HttpsURLConnection
import co.freeside.betamax.proxy.ssl.*

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

			def verifier = new DummyHostNameVerifier()
			HttpsURLConnection.defaultHostnameVerifier = verifier
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
