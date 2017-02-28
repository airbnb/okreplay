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

import com.google.common.io.BaseEncoding
import com.google.common.io.Files
import org.junit.Rule
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import software.betamax.proxy.BetamaxInterceptor
import spock.lang.*

import static Headers.X_BETAMAX
import static java.net.HttpURLConnection.HTTP_OK
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import static java.util.concurrent.TimeUnit.SECONDS
import static software.betamax.MatchRules.*
import static software.betamax.TapeMode.READ_ONLY
import static software.betamax.TapeMode.WRITE_ONLY

@Unroll
@Stepwise
@IgnoreIf({
  def url = "http://httpbin.org/".toURL()
  try {
    HttpURLConnection connection = url.openConnection()
    connection.requestMethod = "HEAD"
    connection.connectTimeout = SECONDS.toMillis(2)
    connection.connect()
    return connection.responseCode >= 400
  } catch (IOException e) {
    System.err.println "Skipping spec as $url is not available"
    return true
  }
})
class BasicAuthSpec extends Specification {

  @Shared private endpoint = "http://httpbin.org/basic-auth/user/passwd".toURL()

  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def configuration = Configuration.builder().tapeRoot(tapeRoot).build()
  @Rule RecorderRule recorder = new RecorderRule(configuration, new BetamaxInterceptor())

  @Betamax(tape = "basic auth", mode = WRITE_ONLY, match = [method, uri, authorization])
  void "can record #status response from authenticated endpoint"() {
    when:
    HttpURLConnection connection = endpoint.openConnection()
    connection.setRequestProperty("Authorization", "Basic $credentials");

    then:
    connection.responseCode == status
    connection.getHeaderField(X_BETAMAX) == "REC"

    where:
    password    | status
    "passwd"    | HTTP_OK
    "INCORRECT" | HTTP_UNAUTHORIZED

    credentials = BaseEncoding.base64().encode("user:$password".bytes)
  }

  @Betamax(tape = "basic auth", mode = READ_ONLY, match = [method, uri, authorization])
  void "can play back #status response from authenticated endpoint"() {
    when:
    HttpURLConnection connection = endpoint.openConnection()
    connection.setRequestProperty("Authorization", "Basic $credentials");

    then:
    connection.responseCode == status
    connection.getHeaderField(X_BETAMAX) == "PLAY"

    where:
    password    | status
    "passwd"    | HTTP_OK
    "INCORRECT" | HTTP_UNAUTHORIZED

    credentials = BaseEncoding.base64().encode("user:$password".bytes)
  }

}
