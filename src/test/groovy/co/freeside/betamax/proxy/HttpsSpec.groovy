package co.freeside.betamax.proxy

import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.proxy.ssl.DummySSLSocketFactory
import groovy.transform.InheritConstructors
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import org.junit.Rule
import co.freeside.betamax.*
import co.freeside.betamax.util.server.*
import org.apache.http.conn.scheme.*
import org.apache.http.params.*
import org.eclipse.jetty.server.*
import spock.lang.*
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.HttpVersion.HTTP_1_1

@Issue("https://github.com/robfletcher/betamax/issues/34")
class HttpsSpec extends Specification {

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Rule @AutoCleanup("ejectTape") Recorder recorder = new Recorder(tapeRoot: tapeRoot, sslSupport: true)
	@Shared @AutoCleanup("stop") SimpleServer httpsEndpoint = new SimpleSecureServer(5001)
	@Shared @AutoCleanup("stop") SimpleServer httpEndpoint = new SimpleServer()

	@Shared URI httpUri
	@Shared URI httpsUri

	HttpClient http

	def setupSpec() {
		httpEndpoint.start(EchoHandler)
		httpsEndpoint.start(HelloHandler)

		httpUri = httpEndpoint.url.toURI()
		httpsUri = httpsEndpoint.url.toURI()
	}

	def setup() {
		def params = new BasicHttpParams()
		HttpProtocolParams.setVersion(params, HTTP_1_1)
		HttpProtocolParams.setContentCharset(params, "UTF-8")

		def registry = new SchemeRegistry()
		registry.register new Scheme("http", PlainSocketFactory.socketFactory, 80)
		registry.register new Scheme("https", DummySSLSocketFactory.instance, 443)

		def connectionManager = new ThreadSafeClientConnManager(params, registry)

		http = new DefaultHttpClient(connectionManager, params)
		http.routePlanner = new ProxySelectorRoutePlanner(http.connectionManager.schemeRegistry, ProxySelector.default)
	}

	@Betamax(tape = "https spec")
	@Unroll("proxy is selected for #scheme URIs")
	def "proxy is selected for all URIs"() {
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

	@Betamax(tape = "https spec", mode = TapeMode.WRITE_ONLY)
	def "proxy can intercept HTTP requests"() {
		when: "an HTTPS request is made"
		def response = http.execute(new HttpGet(httpEndpoint.url))

		then: "it is intercepted by the proxy"
		response.statusLine.statusCode == SC_OK
		response.getFirstHeader(VIA)?.value == "Betamax"
	}

	@Betamax(tape = "https spec", mode = TapeMode.WRITE_ONLY)
	def "proxy can intercept HTTPS requests"() {
		when: "an HTTPS request is made"
		def response = http.execute(new HttpGet(httpsEndpoint.url))
		def responseBytes = new ByteArrayOutputStream()
		response.entity.writeTo(responseBytes)
		def responseString = responseBytes.toString("UTF-8")

		then: "it is intercepted by the proxy"
		response.statusLine.statusCode == SC_OK
		response.getFirstHeader(VIA)?.value == "Betamax"
		responseString == 'Hello World!'
	}

	@Betamax(tape = "https spec", mode = TapeMode.WRITE_ONLY)
	def "https request gets proxied"() {
		expect:
		httpsEndpoint.url.toURL().text == "Hello World!"
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

		def keystore = new File("src/main/resources/keystore").absolutePath

		connector.port = port
		connector.keystore = keystore
		connector.password = "password"
		connector.keyPassword = "password"

		server.connectors = [connector]as Connector[]

		server
	}
}
