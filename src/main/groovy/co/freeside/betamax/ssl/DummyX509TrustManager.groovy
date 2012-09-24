package co.freeside.betamax.ssl

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

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






