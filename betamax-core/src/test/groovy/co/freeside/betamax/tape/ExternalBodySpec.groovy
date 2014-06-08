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

import co.freeside.betamax.io.FilenameNormalizer
import co.freeside.betamax.tape.yaml.YamlTapeLoader
import co.freeside.betamax.util.message.*
import com.google.common.io.Files
import com.google.common.net.MediaType
import spock.lang.*
import static co.freeside.betamax.TapeMode.READ_WRITE
import static co.freeside.betamax.tape.EntityStorage.external
import static com.google.common.net.HttpHeaders.CONTENT_TYPE
import static com.google.common.net.MediaType.JSON_UTF_8
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8
import static com.google.common.net.MediaType.PNG
import static java.net.HttpURLConnection.HTTP_OK

@Issue("https://github.com/robfletcher/betamax/issues/59")
@Unroll
class ExternalBodySpec extends Specification {

    @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
    def loader = new YamlTapeLoader(tapeRoot)

    @Subject def tape = loader.loadTape("external body spec")

    @Shared def request = new BasicRequest("GET", "http://freeside.co/betamax")
    @Shared def plainTextResponse = new BasicResponse(status: 200, reason: "OK", body: "O HAI!".bytes)
    @Shared def imageResponse = new BasicResponse(status: 200, reason: "OK", body: Class.getResourceAsStream("/image.png").bytes)
    @Shared def jsonResponse = new BasicResponse(status: 200, reason: "OK", body: '{"message":"O HAI!"}'.bytes)

    void setup() {
        tape.mode = READ_WRITE

        plainTextResponse.addHeader(CONTENT_TYPE, PLAIN_TEXT_UTF_8.toString())
        imageResponse.addHeader(CONTENT_TYPE, PNG.toString())
        jsonResponse.addHeader(CONTENT_TYPE, JSON_UTF_8.toString())
    }

    void "can write an HTTP interaction to a tape"() {
        given: "the tape is set to record response bodies externally"
        tape.responseBodyStorage = external

        when: "an HTTP interaction is recorded to tape"
        tape.record(request, plainTextResponse)

        then: "the basic request data is stored as normal"
        def interaction = tape.interactions[-1]
        interaction.response.status == plainTextResponse.status
        interaction.response.headers[CONTENT_TYPE] == plainTextResponse.getHeader(CONTENT_TYPE)

        and: "the response body is stored as a file reference"
        interaction.response.body instanceof File
        def body = interaction.response.body as File
        body.isFile()
        body.text == "O HAI!"
    }

    void "the body is written to a subdirectory alongside the tape itself"() {
        given: "the tape is set to record response bodies externally"
        tape.responseBodyStorage = external

        when: "an HTTP interaction is recorded to tape"
        tape.record(request, plainTextResponse)

        then: "the body file is created in the tape root directory"
        def body = tape.interactions[-1].response.body as File
        body.parentFile.name == FilenameNormalizer.toFilename(tape.name)
        body.parentFile.parentFile == tapeRoot
    }

    void "each recorded interaction gets its own file"() {
        given: "the tape is set to record response bodies externally"
        tape.responseBodyStorage = external

        when: "multiple HTTP interactions are recorded to tape"
        tape.record(request, plainTextResponse)
        def request2 = new BasicRequest("GET", "http://freeside.co/betamax/2")
        tape.record(request2, plainTextResponse)

        then: "each interaction has its own body file"
        def body1 = tape.interactions[0].response.body as File
        def body2 = tape.interactions[1].response.body as File
        body1.isFile()
        body2.isFile()
        body1 != body2
    }

    void "if a response is overwritten the body file is re-used"() {
        given: "the tape is set to record response bodies externally"
        tape.responseBodyStorage = external

        and: "an HTTP interaction has already been recorded to tape"
        tape.record(request, plainTextResponse)

        when: "the same interaction is overwritten"
        def secondResponse = new BasicResponse(status: HTTP_OK, reason: "OK", body: "KTHXBYE".bytes)
        secondResponse.addHeader(CONTENT_TYPE, PLAIN_TEXT_UTF_8.toString())
        tape.record(request, secondResponse)

        then: "the body file is re-used"
        tape.interactions[0].response.body == old(tape.interactions[0].response.body)

        and: "the data in the file is replaced"
        (tape.interactions[0].response.body as File).text == "KTHXBYE"
    }

    void "the body file extension is #extension when the content type is #contentType"() {
        given: "the tape is set to record response bodies externally"
        tape.responseBodyStorage = external

        when: "an HTTP interaction is recorded to tape"
        tape.record(request, response)

        then: "the body file is created in the tape root directory"
        def body = tape.interactions[-1].response.body as File
        Files.getFileExtension(body.name) == extension

        where:
        response          | extension
        plainTextResponse | "txt"
        imageResponse     | "png"
        jsonResponse      | "json"

        contentType = response.contentType
    }

    void "the body file is written to YAML as a file path relative to the tape root"() {
        given: "the tape is set to record response bodies externally"
        tape.responseBodyStorage = external

        and: "an HTTP interaction has been recorded to tape"
        tape.record(request, plainTextResponse)

        when: "the tape is written to YAML"
        def writer = new StringWriter()
        loader.writeTo(tape, writer)

        then: "the response body file is a relative path"
        def body = tape.interactions[-1].response.body as File
        writer.toString().contains("body: !file 'external_body_spec${File.separator}$body.name'")
    }

    void "the body file is read from YAML as a file inside the tape root"() {
        given: "a body file stored on disk"
        def body = new File(tapeRoot, "body.txt")
        body.text = "O HAI!"

        and: "a YAML document referring to that file"
        def yaml = """
            !tape
            name: external_body_spec
            interactions:
            - recorded: 2013-11-05T05:50:40.000Z
              request:
                method: GET
                uri: http://freeside.co/betamax
              response:
                status: 200
                headers: {Content-Type: text/plain}
                body: !file 'body.txt'
        """

        when: "the YAML is loaded as a tape"
        def tape = loader.readFrom(new StringReader(yaml))

        then:
        tape.interactions[-1].response.body == body
    }

}
