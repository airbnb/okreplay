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

package software.betamax.proxy

import okhttp3.OkHttpClient
import okhttp3.Request
import software.betamax.Configuration
import software.betamax.Recorder
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

@Issue("https://github.com/robfletcher/betamax/issues/18")
class NoTapeSpec extends Specification {
  @Shared def configuration = Configuration.builder().build()
  @Shared def interceptor = new BetamaxInterceptor()
  @Shared def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()
  @Shared def recorder = new Recorder(configuration, interceptor)
  @Shared @AutoCleanup("stop") def proxy = new ProxyServer(this, interceptor)

  void setupSpec() {
    proxy.start(null)
  }

  void "an error is returned if the proxy intercepts a request when no tape is inserted"() {
    when:
    def request = new Request.Builder()
        .url("http://localhost")
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == 403
    response.body().string() == "No tape"
  }
}
