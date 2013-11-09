/*
 * Copyright 2012 the original author or authors.
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

package co.freeside.betamax.httpclient

import co.freeside.betamax.Configuration
import co.freeside.betamax.handler.HandlerException
import co.freeside.betamax.junit.*
import co.freeside.betamax.util.Network
import co.freeside.betamax.util.server.*
import com.google.common.io.Files
import groovyx.net.http.*
import io.netty.channel.ChannelInboundHandler
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.params.HttpParams
import org.junit.Rule
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_WRITE
import static co.freeside.betamax.util.server.HelloHandler.HELLO_WORLD
import static java.net.HttpURLConnection.HTTP_OK
import static com.google.common.net.HttpHeaders.VIA
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED

@Issue("https://github.com/robfletcher/betamax/issues/40")
class BetamaxHttpClientSpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    def configuration = Spy(Configuration, constructorArgs: [Configuration.builder().tapeRoot(tapeRoot)])
    @Rule RecorderRule recorder = new RecorderRule(configuration)

    @AutoCleanup("stop") def endpoint
    def http = new BetamaxHttpClient(configuration, recorder)

    @Betamax(tape = "betamax http client", mode = READ_WRITE)
    void "can use Betamax without starting the proxy"() {
        given:
        endpoint = SimpleServer.start(HelloHandler)

        and:
        def request = new HttpGet(endpoint.url)

        when:
        def response = http.execute(request)

        then:
        response.statusLine.statusCode == HTTP_OK
        response.entity.content.text == HELLO_WORLD

        and:
        response.getFirstHeader(VIA).value == "Betamax"
        response.getFirstHeader("X-Betamax").value == "REC"
    }

    @Betamax(tape = "betamax http client", mode = READ_WRITE)
    void "can play back from tape"() {
        given:
        def handler = Mock(ChannelInboundHandler)
        endpoint = SimpleServer.start(handler)

        and:
        def request = new HttpGet(endpoint.url)

        when:
        def response = http.execute(request)

        then:
        response.statusLine.statusCode == HTTP_OK
        response.entity.content.text == HELLO_WORLD

        and:
        response.getFirstHeader(VIA).value == "Betamax"
        response.getFirstHeader("X-Betamax").value == "PLAY"

        and:
        0 * handler.channelRead(* _)
    }

    @Betamax(tape = "betamax http client", mode = READ_WRITE)
    void "can send a request with a body"() {
        given:
        endpoint = SimpleServer.start(EchoHandler)

        and:
        def request = new HttpPost(endpoint.url)
        request.entity = new StringEntity("message=O HAI", APPLICATION_FORM_URLENCODED)

        when:
        def response = http.execute(request)

        then:
        response.statusLine.statusCode == HTTP_OK
        response.entity.content.text.endsWith "message=O HAI"

        and:
        response.getFirstHeader(VIA).value == "Betamax"
    }

    void "fails in non-annotated spec"() {
        given:
        def handler = Mock(ChannelInboundHandler)
        endpoint = SimpleServer.start(handler)

        when:
        http.execute(new HttpGet(endpoint.url))

        then:
        def e = thrown(HandlerException)
        e.message == "No tape"

        and:
        0 * handler.channelRead(* _)
    }

    @Betamax(tape = "betamax http client")
    void "can use ignoreLocalhost config setting"() {
        given:
        endpoint = SimpleServer.start(HelloHandler)

        and:
        configuration.isIgnoreLocalhost() >> true

        and:
        def request = new HttpGet(endpoint.url)

        when:
        def response = http.execute(request)

        then:
        response.statusLine.statusCode == HTTP_OK
        response.entity.content.text == HELLO_WORLD

        and:
        !response.getFirstHeader(VIA)
        !response.getFirstHeader("X-Betamax")
    }

    @Betamax(tape = "betamax http client")
    void "can use ignoreHosts config setting"() {
        given:
        endpoint = SimpleServer.start(HelloHandler)

        and:
        configuration.getIgnoreHosts() >> Network.localAddresses

        and:
        def request = new HttpGet(endpoint.url)

        when:
        def response = http.execute(request)

        then:
        response.statusLine.statusCode == HTTP_OK
        response.entity.content.text == HELLO_WORLD

        and:
        !response.getFirstHeader(VIA)
        !response.getFirstHeader("X-Betamax")
    }

    @Betamax(tape = "betamax http client", mode = READ_WRITE)
    void "can use with HttpBuilder"() {
        given:
        endpoint = SimpleServer.start(HelloHandler)

        and:
        def restClient = new RESTClient() {
            @Override
            protected AbstractHttpClient createClient(HttpParams params) {
                new BetamaxHttpClient(configuration, recorder)
            }
        }

        when:
        HttpResponseDecorator response = restClient.get(uri: endpoint.url)

        then:
        response.status == HTTP_OK
        response.data.text == HELLO_WORLD

        and:
        response.getFirstHeader(VIA).value == "Betamax"
        response.getFirstHeader("X-Betamax").value == "PLAY"
    }

}
