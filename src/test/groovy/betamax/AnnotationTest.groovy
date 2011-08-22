package betamax

import betamax.util.EchoServer
import groovyx.net.http.RESTClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import static betamax.server.HttpProxyHandler.X_BETAMAX
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import org.junit.*

class AnnotationTest {

	@Rule public Recorder recorder = Recorder.instance
	EchoServer endpoint = new EchoServer()
    RESTClient http

	@BeforeClass
	static void createTapeDir() {
		Recorder.instance.tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	}

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
		assert Recorder.instance.tapeRoot.deleteDir()
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
		endpoint.start()

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
		endpoint.start()

		def response = http.get(path: "/")

		assert response.status == HTTP_OK
		assert response.getFirstHeader(VIA) == null
	}

}
