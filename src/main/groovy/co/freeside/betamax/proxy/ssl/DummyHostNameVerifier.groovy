package co.freeside.betamax.proxy.ssl

import org.apache.http.conn.ssl.X509HostnameVerifier

import javax.net.ssl.SSLException
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import java.security.cert.X509Certificate

@SuppressWarnings('deprecation')
class DummyHostNameVerifier implements X509HostnameVerifier, com.sun.net.ssl.HostnameVerifier {
	static void useForHttpsURLConnection() {
		def verifier=new DummyHostNameVerifier()
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(verifier)
		com.sun.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(verifier)
	}
	
	boolean verify(String hostname, SSLSession sslSession) {
		true
	}

	void verify(String host, SSLSocket ssl) throws IOException {
		
	}

	void verify(String host, X509Certificate cert) throws SSLException {
		
	}

	void verify(String host, String[] cns, String[] subjectAlts)
			throws SSLException {
		
	}

	boolean verify(String urlHostName, String certHostName) {
		true
	}
}