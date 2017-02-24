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

package software.betamax

import com.google.common.io.Files
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import software.betamax.proxy.BetamaxInterceptor
import spock.lang.Shared

import static Headers.X_BETAMAX
import static TapeMode.READ_WRITE
import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK

@RunWith(OrderedRunner)
class AnnotationTest {
  static def TAPE_ROOT = Files.createTempDir()
  def configuration = Configuration.builder().tapeRoot(TAPE_ROOT).defaultMode(READ_WRITE).build()
  def interceptor = new BetamaxInterceptor(configuration)
  @Rule public RecorderRule recorder = new RecorderRule(configuration, interceptor)
  @Shared static def endpoint = new MockWebServer()

  def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  @BeforeClass
  static void startServer() {
    // The endpoint needs to be shared between all tests otherwise it will be assigned a random
    // port for each test and the rule matching will fail on different URLs
    endpoint.start()
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
    endpoint.enqueue(new MockResponse().setBody("Echo"))
    def request = new Request.Builder()
        .url(endpoint.url("/"))
        .build()
    def response = client.newCall(request).execute()
    assert response.code() == HTTP_OK
    assert response.header(VIA) == 'Betamax'
    assert response.header(X_BETAMAX) == 'REC'
  }

  @Test
  @Betamax(tape = 'annotation_test', mode = READ_WRITE)
  void annotatedTestCanPlayBack() {
    endpoint.enqueue(new MockResponse().setBody("Echo"))
    def request = new Request.Builder()
        .url(endpoint.url("/"))
        .build()
    def response = client.newCall(request).execute()
    assert response.code() == HTTP_OK
    assert response.header(VIA) == 'Betamax'
    assert response.header(X_BETAMAX) == 'PLAY'
  }

  @Test
  void canMakeUnproxiedRequestAfterUsingAnnotation() {
    endpoint.enqueue(new MockResponse().setBody("Echo"))
    def request = new Request.Builder()
        .url(endpoint.url("/"))
        .build()
    def response = client.newCall(request).execute()

    assert response.code() == HTTP_OK
    assert response.header(VIA) == null
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

  @Override protected List<FrameworkMethod> computeTestMethods() {
    super.computeTestMethods().sort(false) { FrameworkMethod o1, FrameworkMethod o2 ->
      ORDER.indexOf(o1.name) <=> ORDER.indexOf(o2.name)
    }
  }
}