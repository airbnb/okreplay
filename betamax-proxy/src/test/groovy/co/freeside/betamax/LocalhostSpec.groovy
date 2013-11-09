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

package co.freeside.betamax

import co.freeside.betamax.junit.*
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import org.junit.ClassRule
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static java.net.HttpURLConnection.HTTP_OK
import static com.google.common.net.HttpHeaders.VIA

@Issue('https://github.com/robfletcher/betamax/issues/62')
@Issue('http://bugs.sun.com/view_bug.do?bug_id=6737819')
@Betamax(mode = WRITE_ONLY)
@Unroll
class LocalhostSpec extends Specification {

    @Shared @AutoCleanup('deleteDir') def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    @Shared @AutoCleanup('stop') def endpoint = new SimpleServer(EchoHandler)

    void setupSpec() {
        endpoint.start()
    }

    @IgnoreIf({ javaVersion >= 1.6 && javaVersion < 1.7 })
    void 'can proxy requests to local endpoint at #uri'() {
        when:
        HttpURLConnection connection = uri.toURL().openConnection()

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == 'Betamax'

        where:
        uri << [endpoint.url, "http://localhost:$endpoint.port/", "http://127.0.0.1:$endpoint.port/"]
    }

}
