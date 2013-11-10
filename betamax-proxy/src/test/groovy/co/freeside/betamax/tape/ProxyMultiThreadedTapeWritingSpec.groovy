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

import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.junit.RecorderRule
import co.freeside.betamax.tck.MultiThreadedTapeWritingSpec
import org.junit.Rule

class ProxyMultiThreadedTapeWritingSpec extends MultiThreadedTapeWritingSpec {

    def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).build()
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    @Override
    protected String makeRequest(String url) {
        url.toURL().text
    }
}
