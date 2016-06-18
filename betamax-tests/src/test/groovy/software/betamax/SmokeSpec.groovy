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

import org.junit.ClassRule
import software.betamax.encoding.GzipEncoder
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import spock.lang.*

import javax.net.ssl.HttpsURLConnection

import static Headers.X_BETAMAX
import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK

@Unroll
@Betamax
class SmokeSpec extends Specification {

    static final TAPE_ROOT = new File(SmokeSpec.getResource("/betamax/tapes").toURI())
    @Shared def configuration = Configuration.builder().sslEnabled(true).tapeRoot(TAPE_ROOT).build()
    @Shared @ClassRule RecorderRule recorder = new RecorderRule(configuration)

    void "#type response data"() {
        when:
        HttpURLConnection connection = uri.toURL().openConnection()
        connection.setRequestProperty("Accept-Encoding", "gzip")

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "PLAY"
        connection.inputStream.text.contains(expectedContent)

        where:
        type   | uri                             | expectedContent
        "txt"  | "http://httpbin.org/robots.txt" | "User-agent: *"
        "html" | "http://httpbin.org/html"       | "<!DOCTYPE html>"
        "json" | "http://httpbin.org/get"        | '"url": "http://httpbin.org/get"'
    }

    void "gzipped response data"() {
        when:
        HttpURLConnection connection = uri.toURL().openConnection()
        connection.setRequestProperty("Accept-Encoding", "gzip")

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "PLAY"
        encoder.decode(connection.inputStream).contains('"gzipped": true')

        where:
        uri = "http://httpbin.org/gzip"
        encoder = new GzipEncoder()
    }

    void "redirects are followed"() {
        when:
        HttpURLConnection connection = uri.toURL().openConnection()

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "PLAY"
        connection.inputStream.text.contains('"url": "http://httpbin.org/get"')

        where:
        uri = "http://httpbin.org/redirect/1"
    }

    void "https proxying"() {
        when:
        HttpsURLConnection connection = uri.toURL().openConnection()

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "PLAY"
        connection.inputStream.text.contains('"url": "http://httpbin.org/get"')

        where:
        uri = "https://httpbin.org/get"
    }

    @Ignore
    void "can POST to https"() {
        when:
        HttpsURLConnection connection = uri.toURL().openConnection()
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.outputStream.withStream {
            it << "message=O HAI"
        }

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "PLAY"
        connection.inputStream.text.contains('"message": "O HAI"')

        where:
        uri = "https://httpbin.org/post"
    }

    @Issue(["https://github.com/robfletcher/betamax/issues/61", "http://jira.codehaus.org/browse/JETTY-1533"])
    void "can cope with URLs that do not end in a slash"() {
        when:
        HttpURLConnection connection = uri.toURL().openConnection()

        then:
        connection.responseCode == HTTP_OK
        connection.getHeaderField(VIA) == "Betamax"
        connection.getHeaderField(X_BETAMAX) == "PLAY"

        where:
        uri = "http://httpbin.org"
    }
}
