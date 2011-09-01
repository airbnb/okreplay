package betamax

import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import static betamax.server.HttpProxyHandler.X_BETAMAX
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import org.junit.*
import betamax.util.SimpleServer
import betamax.util.EchoHandler

class AnnotationTest {

	static File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Rule public Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	SimpleServer endpoint = new SimpleServer()
	RESTClient http

	@Before
	void initRestClient() {
		http = new RESTClient(endpoint.url)
		http.client.routePlanner = new ProxySelectorRoutePlanner(http.client.connectionManager.schemeRegistry, ProxySelector.default)
	}

	@After
	void ensureEndpointIsStopped() {
		endpoint.stop()
	}

	@AfterClass
	static void cleanUpTapeFiles() {
		tapeRoot.deleteDir()
	}

	@Test
	void noTapeIsInsertedIfThereIsNoAnnotationOnTheTest() {
		assert recorder.tape == null
	}

	@Test
	@Betamax(tape = "annotation_test")
	void annotationOnTestCausesTapeToBeInserted() {
		assert recorder.tape.name == "annotation_test"
	}

	@Test
	void tapeIsEjectedAfterAnnotatedTestCompletes() {
		assert recorder.tape == null
	}

	@Test
	@Betamax(tape = "annotation_test")
	void annotatedTestCanRecord() {
		endpoint.start(EchoHandler)

		def response = http.get(path: "/")

		assert response.status == HTTP_OK
		assert response.getFirstHeader(VIA)?.value == "Betamax"
		assert response.getFirstHeader(X_BETAMAX)?.value == "REC"
	}

	@Test
	@Betamax(tape = "annotation_test")
	void annotatedTestCanPlayBack() {
		def response = http.get(path: "/")

		assert response.status == HTTP_OK
		assert response.getFirstHeader(VIA)?.value == "Betamax"
		assert response.getFirstHeader(X_BETAMAX)?.value == "PLAY"
	}

	@Test
	void canMakeUnproxiedRequestAfterUsingAnnotation() {
		endpoint.start(EchoHandler)

		def response = http.get(path: "/")

		assert response.status == HTTP_OK
		assert response.getFirstHeader(VIA) == null
	}

}
