package co.freeside.betamax.util

import java.security.Security
import co.freeside.betamax.proxy.ssl.*
import spock.lang.Specification
import static co.freeside.betamax.util.SSLOverrider.SSL_SOCKET_FACTORY_PROVIDER

class SSLOverriderSpec extends Specification {

	SSLOverrider sslOverrider = new SSLOverrider()

	void 'overrides SSL settings when activated'() {
		when:
		sslOverrider.activate()

		then:
		Security.getProperty(SSL_SOCKET_FACTORY_PROVIDER) ==  DummyJVMSSLSocketFactory.name
		javax.net.ssl.HttpsURLConnection.defaultHostnameVerifier instanceof DummyHostNameVerifier

		cleanup:
		sslOverrider.deactivate()
	}

	void 'restores original SSL settings when deactivated'() {
		when:
		sslOverrider.activate()
		sslOverrider.deactivate()

		then:
		Security.getProperty(SSL_SOCKET_FACTORY_PROVIDER) ==  old(Security.getProperty(SSL_SOCKET_FACTORY_PROVIDER))
		!(javax.net.ssl.HttpsURLConnection.defaultHostnameVerifier instanceof DummyHostNameVerifier)
	}

}
