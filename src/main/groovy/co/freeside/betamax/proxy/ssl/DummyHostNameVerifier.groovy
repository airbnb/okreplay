package co.freeside.betamax.proxy.ssl

import org.apache.http.conn.ssl.X509HostnameVerifier

import javax.net.ssl.SSLException
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import java.security.cert.X509Certificate

@SuppressWarnings("deprecation")
class DummyHostNameVerifier implements X509HostnameVerifier, com.sun.net.ssl.HostnameVerifier {
	public static void useForHttpsURLConnection() {
		def verifier=new DummyHostNameVerifier()
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(verifier)
		com.sun.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(verifier)
	}
	
	public boolean verify(String hostname, SSLSession sslSession) {
		true
	}

	public void verify(String host, SSLSocket ssl) throws IOException {
		
	}

	public void verify(String host, X509Certificate cert) throws SSLException {
		
	}

	public void verify(String host, String[] cns, String[] subjectAlts)
			throws SSLException {
		
	}

	public boolean verify(String urlHostName, String certHostName) {
		true
	}
}