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

//import com.google.common.io.Files
//import org.junit.Rule
//import software.betamax.junit.Betamax
//import software.betamax.android.RecorderRule
//import software.betamax.util.server.EchoHandler
//import software.betamax.util.server.SimpleServer
//import spock.lang.AutoCleanup
//import spock.lang.Shared
//import spock.lang.Specification
//import spock.lang.Stepwise
//
//import static Headers.X_BETAMAX
//import static TapeMode.READ_WRITE
//import static TapeMode.WRITE_ONLY
//import static com.google.common.net.HttpHeaders.VIA
//import static java.net.HttpURLConnection.HTTP_OK
//
//@Stepwise
//class AnnotationSpec extends Specification {
//
//  @Shared @AutoCleanup('deleteDir') def tapeRoot = Files.createTempDir()
//  @Shared def configuration = Configuration.builder().tapeRoot(tapeRoot).build()
//  @Rule RecorderRule recorder = new RecorderRule(configuration)
//
//  @AutoCleanup('stop') def endpoint = new SimpleServer(EchoHandler)
//
//  void 'no tape is inserted if there is no annotation on the feature'() {
//    expect:
//    recorder.tape == null
//  }
//
//  @Betamax(tape = 'annotation_spec', mode = WRITE_ONLY, match = [MatchRules.body])
//  void 'annotation on feature causes tape to be inserted'() {
//    expect:
//    recorder.tape.name == 'annotation_spec'
//    recorder.tape.mode == WRITE_ONLY
//    recorder.tape.matchRule == ComposedMatchRule.of(MatchRules.body)
//  }
//
//  void 'tape is ejected after annotated feature completes'() {
//    expect:
//    recorder.tape == null
//  }
//
//  @Betamax(tape = 'annotation_spec', mode = READ_WRITE)
//  void 'annotated feature can record'() {
//    given:
//    endpoint.start()
//
//    when:
//    HttpURLConnection connection = endpoint.url.toURL().openConnection()
//
//    then:
//    connection.responseCode == HTTP_OK
//    connection.getHeaderField(VIA) == 'Betamax'
//    connection.getHeaderField(X_BETAMAX) == 'REC'
//  }
//
//  @Betamax(tape = 'annotation_spec', mode = READ_WRITE)
//  void 'annotated feature can play back'() {
//    when:
//    HttpURLConnection connection = endpoint.url.toURL().openConnection()
//
//    then:
//    connection.responseCode == HTTP_OK
//    connection.getHeaderField(VIA) == 'Betamax'
//    connection.getHeaderField(X_BETAMAX) == 'PLAY'
//  }
//
//  void 'can make unproxied request after using annotation'() {
//    given:
//    endpoint.start()
//
//    when:
//    HttpURLConnection connection = endpoint.url.toURL().openConnection()
//
//    then:
//    connection.responseCode == HTTP_OK
//    connection.getHeaderField(VIA) == null
//  }
//
//}
