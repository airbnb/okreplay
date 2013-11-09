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

import co.freeside.betamax.junit.*
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.TapeMode.READ_WRITE
import static java.net.HttpURLConnection.HTTP_OK
import static com.google.common.net.HttpHeaders.VIA

@RunWith(OrderedRunner)
class AnnotationTest {

    static def TAPE_ROOT = Files.createTempDir()
    def configuration = ProxyConfiguration.builder().tapeRoot(TAPE_ROOT).defaultMode(READ_WRITE).build()
    @Rule public RecorderRule recorder = new RecorderRule(configuration)

    def endpoint = new SimpleServer(EchoHandler)

    @After
    void ensureEndpointIsStopped() {
        endpoint.stop()
    }

    @AfterClass
    static void cleanUpTapeFiles() {
        TAPE_ROOT.deleteDir()
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
        endpoint.start()

        HttpURLConnection connection = endpoint.url.toURL().openConnection()

        assert connection.responseCode == HTTP_OK
        assert connection.getHeaderField(VIA) == 'Betamax'
        assert connection.getHeaderField(X_BETAMAX) == 'REC'
    }

    @Test
    @Betamax(tape = 'annotation_test', mode = READ_WRITE)
    void annotatedTestCanPlayBack() {
        HttpURLConnection connection = endpoint.url.toURL().openConnection()

        assert connection.responseCode == HTTP_OK
        assert connection.getHeaderField(VIA) == 'Betamax'
        assert connection.getHeaderField(X_BETAMAX) == 'PLAY'
    }

    @Test
    void canMakeUnproxiedRequestAfterUsingAnnotation() {
        endpoint.start()

        HttpURLConnection connection = endpoint.url.toURL().openConnection()

        assert connection.responseCode == HTTP_OK
        assert connection.getHeaderField(VIA) == null
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
        super.computeTestMethods().sort { FrameworkMethod o1, FrameworkMethod o2 ->
            ORDER.indexOf(o1.name) <=> ORDER.indexOf(o2.name)
        }
    }
}