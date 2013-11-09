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

import co.freeside.betamax.*
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_WRITE
import static com.google.common.net.HttpHeaders.VIA

@Issue("https://github.com/robfletcher/betamax/issues/16")
@Unroll
class IgnoreHostsSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") File tapeRoot = Files.createTempDir()
    def configuration = Spy(ProxyConfiguration, constructorArgs: [ProxyConfiguration.builder().tapeRoot(tapeRoot).defaultMode(READ_WRITE)])
    @AutoCleanup("stop") Recorder recorder = new Recorder(configuration)

    @Shared @AutoCleanup("stop") SimpleServer endpoint = new SimpleServer(EchoHandler)

    void setupSpec() {
        endpoint.start()
    }

    void "does not proxy a request to #requestURI when ignoring #ignoreHosts"() {
        given: "proxy is configured to ignore local connections"
        configuration.getIgnoreHosts() >> [ignoreHosts]
        recorder.start("ignore hosts spec")

        when: "a request is made"
        HttpURLConnection connection = requestURI.toURL().openConnection()

        then: "the request is not intercepted by the proxy"
        connection.getHeaderField(VIA) == null

        and: "nothing is recorded to the tape"
        recorder.tape.size() == old(recorder.tape.size())

        where:
        ignoreHosts               | requestURI
        endpoint.url.toURI().host | endpoint.url
        "localhost"               | "http://localhost:${endpoint.url.toURI().port}"
        "127.0.0.1"               | "http://localhost:${endpoint.url.toURI().port}"
        endpoint.url.toURI().host | "http://localhost:${endpoint.url.toURI().port}"
    }

    void "does not proxy a request to #requestURI when ignoreLocalhost is true"() {
        given: "proxy is configured to ignore local connections"
        configuration.ignoreLocalhost >> true
        recorder.start("ignore hosts spec")

        when: "a request is made"
        HttpURLConnection connection = requestURI.toURL().openConnection()

        then: "the request is not intercepted by the proxy"
        connection.getHeaderField(VIA) == null

        and: "nothing is recorded to the tape"
        recorder.tape.size() == old(recorder.tape.size())

        where:
        requestURI << [endpoint.url, "http://localhost:${endpoint.url.toURI().port}"]
    }

}
