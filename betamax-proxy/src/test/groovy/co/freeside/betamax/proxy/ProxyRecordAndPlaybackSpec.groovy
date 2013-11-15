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
import org.yaml.snakeyaml.Yaml
import spock.lang.*
import static co.freeside.betamax.Headers.X_BETAMAX
import static co.freeside.betamax.TapeMode.READ_WRITE
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK

@Stepwise
@Timeout(10)
class ProxyRecordAndPlaybackSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).defaultMode(READ_WRITE).build()
    @Shared def recorder = new Recorder(configuration)
    @AutoCleanup("stop") def endpoint = new SimpleServer(HelloHandler)

    void setupSpec() {
        recorder.start("proxy record and playback spec")
    }

    void cleanupSpec() {
        try {
            recorder.stop()
        } catch (IllegalStateException e) {
            // recorder was already stopped
        }
    }

    void "proxy makes a real HTTP request the first time it gets a request for a URI"() {
        given:
        endpoint.start()

        when:
        HttpURLConnection connection = endpoint.url.toURL().openConnection()

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "REC"
        connection.inputStream.text == HELLO_WORLD

        and:
        recorder.tape.size() == 1
    }

    void "subsequent requests for the same URI are played back from tape"() {
        when:
        HttpURLConnection connection = endpoint.url.toURL().openConnection()

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "PLAY"
        connection.inputStream.text == HELLO_WORLD

        and:
        recorder.tape.size() == 1
    }

    void "subsequent requests with a different HTTP method are recorded separately"() {
        given:
        endpoint.start()

        when:
        HttpURLConnection connection = endpoint.url.toURL().openConnection()
        connection.requestMethod = "POST"

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "REC"
        connection.inputStream.text == HELLO_WORLD

        and:
        recorder.tape.size() == 2
        recorder.tape.interactions[-1].request.method == "POST"
    }

    void "when the tape is ejected the data is written to a file"() {
        when:
        recorder.stop()

        then:
        def file = new File(tapeRoot, "proxy_record_and_playback_spec.yaml")
        file.isFile()

        and:
        def yaml = file.withReader { reader ->
            new Yaml().loadAs(reader, Map)
        }
        yaml.name == "proxy record and playback spec"
        yaml.interactions.size() == 2
    }

    void "can load an existing tape from a file"() {
        given:
        def file = new File(tapeRoot, "existing_tape.yaml")
        file.parentFile.mkdirs()
        file.text = """\
!tape
name: existing_tape
interactions:
- recorded: 2011-08-19T11:45:33.000Z
  request:
    method: GET
    uri: http://icanhascheezburger.com/
    headers: {Accept-Language: "en-GB,en", If-None-Match: b00b135}
  response:
    status: 200
    headers: {Content-Type: text/plain, Content-Language: en-GB}
    body: O HAI!
"""

        when:
        recorder.start("existing_tape")

        then:
        recorder.tape.name == "existing_tape"
        recorder.tape.size() == 1
    }

    void "can play back a loaded tape"() {
        when:
        HttpURLConnection connection = "http://icanhascheezburger.com/".toURL().openConnection()

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "PLAY"
        connection.inputStream.text == "O HAI!"
    }

}
