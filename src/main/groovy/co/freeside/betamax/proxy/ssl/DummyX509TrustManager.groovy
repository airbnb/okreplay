package co.freeside.betamax.proxy.ssl

import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

class DummyX509TrustManager implements X509TrustManager {
	void checkClientTrusted(X509Certificate[] chain, String authType) {
	}

	void checkServerTrusted(X509Certificate[] chain, String authType) {
	}

	X509Certificate[] getAcceptedIssuers() {
		null
	}
}






