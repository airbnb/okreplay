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

package co.freeside.betamax.tape

import co.freeside.betamax.Configuration
import co.freeside.betamax.httpclient.BetamaxHttpClient
import co.freeside.betamax.junit.RecorderRule
import co.freeside.betamax.tck.MultiThreadedTapeWritingSpec
import org.apache.http.client.methods.HttpGet
import org.junit.Rule

class HttpClientMultiThreadedTapeWritingSpec extends MultiThreadedTapeWritingSpec {

    def configuration = Configuration.builder().tapeRoot(tapeRoot).build()
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    def httpClient = new BetamaxHttpClient(configuration, recorder)

    protected String makeRequest(String url) {
        def request = new HttpGet(url)
        def response = httpClient.execute(request)
        response.entity.content.text
    }
}
