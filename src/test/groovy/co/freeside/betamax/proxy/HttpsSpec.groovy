package co.freeside.betamax.proxy

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.TapeMode
import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import co.freeside.betamax.util.server.EchoHandler
import co.freeside.betamax.util.server.HelloHandler
import groovy.transform.InheritConstructors
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpProtocolParams
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import org.junit.Rule
import spock.lang.*

import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.HttpVersion.HTTP_1_1

@Issue('https://github.com/robfletcher/betamax/issues/34')
class HttpsSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = new File(System.properties.'java.io.tmpdir', 'tapes')
	@Rule @AutoCleanup('ejectTape') Recorder recorder = new Recorder(tapeRoot: tapeRoot, sslSupport: true)
	@Shared @AutoCleanup('stop') SimpleServer httpsEndpoint = new SimpleSecureServer(5001)
	@Shared @AutoCleanup('stop') SimpleServer httpEndpoint = new SimpleServer()

	@Shared URI httpUri
	@Shared URI httpsUri

	HttpClient http

	void setupSpec() {
		httpEndpoint.start(EchoHandler)
		httpsEndpoint.start(HelloHandler)

		httpUri = httpEndpoint.url.toURI()
		httpsUri = httpsEndpoint.url.toURI()
	}

	void setup() {
		def params = new BasicHttpParams()
		HttpProtocolParams.setVersion(params, HTTP_1_1)
		HttpProtocolParams.setContentCharset(params, 'UTF-8')

		def registry = new SchemeRegistry()
		registry.register new Scheme('http', PlainSocketFactory.socketFactory, 80)
		registry.register new Scheme('https', DummySSLSocketFactory.instance, 443)

		def connectionManager = new ThreadSafeClientConnManager(params, registry)

		http = new DefaultHttpClient(connectionManager, params)
		BetamaxRoutePlanner.configure(http)
	}

	@Betamax(tape = 'https spec')
	@Unroll('proxy is selected for #scheme URIs')
	void 'proxy is selected for all URIs'() {
		given:
		def proxySelector = ProxySelector.default

		expect:
		def proxy = proxySelector.select(uri).first()
		proxy.type() == Proxy.Type.HTTP
		proxy.address().toString() == "$recorder.proxyHost:${scheme == 'https' ? recorder.httpsProxyPort : recorder.proxyPort}"

		where:
		uri << [httpUri, httpsUri]
		scheme = uri.scheme
	}

	@Betamax(tape = 'https spec', mode = TapeMode.WRITE_ONLY)
	void 'proxy can intercept HTTP requests'() {
		when: 'an HTTPS request is made'
		def response = http.execute(new HttpGet(httpEndpoint.url))

		then: 'it is intercepted by the proxy'
		response.statusLine.statusCode == SC_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
	}

	@Betamax(tape = 'https spec', mode = TapeMode.WRITE_ONLY)
	void 'proxy can intercept HTTPS requests'() {
		when: 'an HTTPS request is made'
		def response = http.execute(new HttpGet(httpsEndpoint.url))
		def responseBytes = new ByteArrayOutputStream()
		response.entity.writeTo(responseBytes)
		def responseString = responseBytes.toString('UTF-8')

		then: 'it is intercepted by the proxy'
		response.statusLine.statusCode == SC_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
		responseString == 'Hello World!'
	}

	@Betamax(tape = 'https spec', mode = TapeMode.WRITE_ONLY)
	void 'https request gets proxied'() {
		expect:
		httpsEndpoint.url.toURL().text == 'Hello World!'
	}
}


@InheritConstructors
class SimpleSecureServer extends SimpleServer {

	@Override
	String getUrl() {
		"https://$host:$port/"
	}

	@Override
	protected Server createServer(int port) {
		def server = super.createServer(port)

		def connector = new SslSelectChannelConnector()

		def keystore = SimpleSecureServer.getResource('/betamax.keystore')

		connector.port = port
		connector.keystore = keystore
		connector.password = 'password'
		connector.keyPassword = 'password'

		server.connectors = [connector]as Connector[]

		server
	}
}
