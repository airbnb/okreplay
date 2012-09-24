package co.freeside.betamax.proxy.ssl

import org.apache.http.conn.ssl.X509HostnameVerifier

import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import java.security.cert.X509Certificate

@SuppressWarnings('deprecation')
class DummyHostNameVerifier implements X509HostnameVerifier, com.sun.net.ssl.HostnameVerifier {


	@Override
	boolean verify(String hostname, SSLSession sslSession) {
		true
	}

	@Override
	void verify(String host, SSLSocket ssl) { }

	@Override
	void verify(String host, X509Certificate cert) { }

	@Override
	void verify(String host, String[] cns, String[] subjectAlts) { }

	@Override
	boolean verify(String urlHostName, String certHostName) {
		true
	}
}