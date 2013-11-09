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

package co.freeside.betamax.tape

import co.freeside.betamax.ProxyConfiguration
import co.freeside.betamax.Recorder
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.yaml.snakeyaml.Yaml
import spock.lang.*
import static co.freeside.betamax.TapeMode.WRITE_ONLY
import static com.google.common.base.Charsets.UTF_8
import static com.google.common.net.HttpHeaders.CONTENT_TYPE
import static com.google.common.net.MediaType.JSON_UTF_8
import static java.util.concurrent.TimeUnit.SECONDS

@Issue("https://github.com/robfletcher/betamax/issues/50")
@IgnoreIf({
    def url = "http://httpbin.org/".toURL()
    try {
        HttpURLConnection connection = url.openConnection()
        connection.requestMethod = "HEAD"
        connection.connectTimeout = SECONDS.toMillis(2)
        connection.connect()
        return connection.responseCode >= 400
    } catch (IOException e) {
        System.err.println "Skipping spec as $url is not available"
        return true
    }
})
class PostBodySpec extends Specification {

    @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    @Shared def configuration = ProxyConfiguration.builder().tapeRoot(tapeRoot).build()
    def recorder = new Recorder(configuration)

    def httpClient = HttpClients.createSystem()

    void "post body is stored on tape when using UrlConnection"() {
        given:
        def postBody = '{"foo":"bar"}'
        HttpURLConnection connection = "http://httpbin.org/post".toURL().openConnection()
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.addRequestProperty(CONTENT_TYPE, JSON_UTF_8.toString())

        and:
        recorder.start("post_body_with_url_connection", WRITE_ONLY)

        when:
        connection.outputStream.withStream { stream ->
            stream << postBody.getBytes(UTF_8)
        }
        println connection.inputStream.text // response body must be consumed

        and:
        recorder.stop()

        then:
        def file = new File(tapeRoot, "post_body_with_url_connection.yaml")
        def tapeData = file.withReader {
            new Yaml().loadAs(it, Map)
        }
        tapeData.interactions[0].request.body == postBody
    }

    void "post body is stored on tape when using HttpClient"() {
        given:
        def postBody = '{"foo":"bar"}'
        def httpPost = new HttpPost("http://httpbin.org/post")
        httpPost.setHeader(CONTENT_TYPE, JSON_UTF_8.toString())
        def reqEntity = new StringEntity(postBody, UTF_8)
        reqEntity.setContentType(JSON_UTF_8.toString())
        httpPost.entity = reqEntity

        and:
        recorder.start("post_body_with_http_client", WRITE_ONLY)

        when:
        httpClient.execute(httpPost)

        and:
        recorder.stop()

        then:
        def file = new File(tapeRoot, "post_body_with_http_client.yaml")
        def tapeData = file.withReader {
            new Yaml().loadAs(it, Map)
        }
        tapeData.interactions[0].request.body == postBody
    }

}
