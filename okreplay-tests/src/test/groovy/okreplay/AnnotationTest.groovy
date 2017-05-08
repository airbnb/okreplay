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

package okreplay

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
import spock.lang.Shared

import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK
import static okreplay.Headers.X_OKREPLAY
import static okreplay.TapeMode.READ_WRITE

@RunWith(OrderedRunner)
class AnnotationTest {
  static def TAPE_ROOT = Files.createTempDir()
  def configuration = new OkReplayConfig.Builder()
      .tapeRoot(TAPE_ROOT)
      .defaultMode(READ_WRITE)
      .interceptor(new OkReplayInterceptor())
      .build()
  @Rule public RecorderRule recorder = new RecorderRule(configuration)
  @Shared static def endpoint = new MockWebServer()

  def client = new OkHttpClient.Builder()
      .addInterceptor(configuration.interceptor())
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
  @OkReplay(tape = 'annotation_test', mode = READ_WRITE)
  void annotationOnTestCausesTapeToBeInserted() {
    assert recorder.tape.name == 'annotation_test'
  }

  @Test
  void tapeIsEjectedAfterAnnotatedTestCompletes() {
    assert recorder.tape == null
  }

  @Test
  @OkReplay(tape = 'annotation_test', mode = READ_WRITE)
  void annotatedTestCanRecord() {
    endpoint.enqueue(new MockResponse().setBody("Echo"))
    def request = new Request.Builder()
        .url(endpoint.url("/"))
        .build()
    def response = client.newCall(request).execute()
    assert response.code() == HTTP_OK
    assert response.header(VIA) == 'OkReplay'
    assert response.header(X_OKREPLAY) == 'REC'
  }

  @Test
  @OkReplay(tape = 'annotation_test', mode = READ_WRITE)
  void annotatedTestCanPlayBack() {
    endpoint.enqueue(new MockResponse().setBody("Echo"))
    def request = new Request.Builder()
        .url(endpoint.url("/"))
        .build()
    def response = client.newCall(request).execute()
    assert response.code() == HTTP_OK
    assert response.header(VIA) == 'OkReplay'
    assert response.header(X_OKREPLAY) == 'PLAY'
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
