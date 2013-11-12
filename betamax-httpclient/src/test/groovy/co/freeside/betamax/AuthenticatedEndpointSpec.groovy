/*
 * Copyright 2013 the original author or authors.
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

import co.freeside.betamax.httpclient.BetamaxHttpClient
import co.freeside.betamax.junit.*
import com.google.common.io.Files
import org.apache.http.auth.*
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicCredentialsProvider
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static java.net.HttpURLConnection.HTTP_OK

@Issue("https://github.com/robfletcher/betamax/issues/100")
class AuthenticatedEndpointSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    def configuration = Configuration.builder().tapeRoot(tapeRoot).defaultMode(WRITE_ONLY).build()
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    def http = new BetamaxHttpClient(configuration, recorder)

    @Betamax
    void "can connect to an authenticated endpoint"() {
        given:
        def credentials = new UsernamePasswordCredentials("user", "passwd")
        def credentialsProvider = new BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, credentials)
        http.setCredentialsProvider(credentialsProvider)

        when:
        def request = new HttpGet(url)
        def response = http.execute(request)

        then:
        response.statusLine.statusCode == HTTP_OK

        where:
        url = "http://httpbin.org/basic-auth/user/passwd"
    }

}
