package co.freeside.betamax.proxy.ssl

import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

class DummyX509TrustManager implements X509TrustManager {

	@Override
	void checkClientTrusted(X509Certificate[] chain, String authType) { }

	@Override
	void checkServerTrusted(X509Certificate[] chain, String authType) { }

	@Override
	X509Certificate[] getAcceptedIssuers() {
		null
	}
}






