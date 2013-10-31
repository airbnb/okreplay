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

package co.freeside.betamax.proxy

import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.junit.*
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import org.junit.ClassRule
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_WRITE

@Issue("https://github.com/robfletcher/betamax/issues/20")
@Issue("https://github.com/adamfisk/LittleProxy/issues/96")
@Betamax(mode = READ_WRITE)
class ProxyTimeoutSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().proxyTimeoutSeconds(1).tapeRoot(tapeRoot).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    @AutoCleanup("stop") def endpoint = new SimpleServer(SlowHandler)

    void "proxy responds with 504 if target server takes too long to respond"() {
        given:
        endpoint.start()

        when:
        HttpURLConnection connection = endpoint.url.toURL().openConnection()
        connection.inputStream.text

        then:
        def e = thrown(IOException)
        e.message == "Server returned HTTP response code: 504 for URL: http://localhost:5000/"
    }

}
