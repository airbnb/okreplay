/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax

import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.httpbuilder.BetamaxRESTClient
import co.freeside.betamax.util.server.EchoHandler
import groovyx.net.http.RESTClient
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.TapeMode.READ_WRITE
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA

@RunWith(OrderedRunner)
class AnnotationTest {

	static File tapeRoot = newTempDir('tapes')
	@Rule public Recorder recorder = new ProxyRecorder(tapeRoot: tapeRoot, defaultMode: READ_WRITE)
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
	@Betamax(tape = 'annotation_test', mode = READ_WRITE)
	void annotationOnTestCausesTapeToBeInserted() {
		assert recorder.tape.name == 'annotation_test'
	}

	@Test
	void tapeIsEjectedAfterAnnotatedTestCompletes() {
		assert recorder.tape == null
	}

	@Test
	@Betamax(tape = 'annotation_test', mode = READ_WRITE)
	void annotatedTestCanRecord() {
		endpoint.start(EchoHandler)

		def response = http.get(path: '/')

		assert response.status == HTTP_OK
		assert response.getFirstHeader(VIA)?.value == 'Betamax'
		assert response.getFirstHeader(X_BETAMAX)?.value == 'REC'
	}

	@Test
	@Betamax(tape = 'annotation_test', mode = READ_WRITE)
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