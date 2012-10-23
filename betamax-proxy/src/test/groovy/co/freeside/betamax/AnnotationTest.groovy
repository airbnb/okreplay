package co.freeside.betamax

import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.RESTClient
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import static co.freeside.betamax.proxy.jetty.BetamaxProxy.X_BETAMAX
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@RunWith(OrderedRunner)
class AnnotationTest {

	static File tapeRoot = newTempDir('tapes')
	@Rule public Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	SimpleServer endpoint = new SimpleServer()
	RESTClient http

	@Before
	void initRestClient() {
		http = new BetamaxRESTClient(endpoint.url)
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
	@Betamax(tape = 'annotation_test')
	void annotationOnTestCausesTapeToBeInserted() {
		assert recorder.tape.name == 'annotation_test'
	}

	@Test
	void tapeIsEjectedAfterAnnotatedTestCompletes() {
		assert recorder.tape == null
	}

	@Test
	@Betamax(tape = 'annotation_test')
	void annotatedTestCanRecord() {
		endpoint.start(EchoHandler)

		def response = http.get(path: '/')

		assert response.status == HTTP_OK
		assert response.getFirstHeader(VIA)?.value == 'Betamax'
		assert response.getFirstHeader(X_BETAMAX)?.value == 'REC'
	}

	@Test
	@Betamax(tape = 'annotation_test')
	void annotatedTestCanPlayBack() {
		def response = http.get(path: '/')

		assert response.status == HTTP_OK
		assert response.getFirstHeader(VIA)?.value == 'Betamax'
		assert response.getFirstHeader(X_BETAMAX)?.value == 'PLAY'
	}

	@Test
	void canMakeUnproxiedRequestAfterUsingAnnotation() {
		endpoint.start(EchoHandler)

		def response = http.get(path: '/')

		assert response.status == HTTP_OK
		assert response.getFirstHeader(VIA) == null
	}

}

/**
 * This is an evil hack. JUnit does not guarantee test execution order and the methods in this test depend on each
 * other. In particular `annotatedTestCanPlayBack` will fail if run before `annotatedTestCanRecord`. Really the tests
 * should be idempotent.
 */
class OrderedRunner extends BlockJUnit4ClassRunner {

	private static final ORDER = [
			'noTapeIsInsertedIfThereIsNoAnnotationOnTheTest',
			'annotationOnTestCausesTapeToBeInserted',
			'tapeIsEjectedAfterAnnotatedTestCompletes',
			'annotatedTestCanRecord',
			'annotatedTestCanPlayBack',
			'canMakeUnproxiedRequestAfterUsingAnnotation'
	]

	OrderedRunner(Class testClass) {
		super(testClass)
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		super.computeTestMethods().sort {FrameworkMethod o1, FrameworkMethod o2 ->
			ORDER.indexOf(o1.name) <=> ORDER.indexOf(o2.name)
		}
	}
}