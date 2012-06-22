package co.freeside.betamax.proxy.ssl

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class DummyX509TrustManager implements X509TrustManager {
	void checkClientTrusted(X509Certificate[] chain, String authType) {
	}

	void checkServerTrusted(X509Certificate[] chain, String authType) {
	}

	X509Certificate[] getAcceptedIssuers() {
		null
	}
}






