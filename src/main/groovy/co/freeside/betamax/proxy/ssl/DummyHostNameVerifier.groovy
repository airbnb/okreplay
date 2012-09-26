package co.freeside.betamax.proxy.ssl

import java.security.cert.X509Certificate
import javax.net.ssl.*
import org.apache.http.conn.ssl.X509HostnameVerifier

class DummyHostNameVerifier implements X509HostnameVerifier {

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

}