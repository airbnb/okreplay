package walkman

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.yaml.snakeyaml.Yaml
import walkman.RecordedRequest
import walkman.RecordedResponse
import walkman.YamlTapeLoader
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static com.google.common.net.HttpHeaders.*
import static com.google.common.net.MediaType.FORM_DATA
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST
import static java.net.HttpURLConnection.HTTP_OK
import static walkman.TapeMode.READ_WRITE

class WriteTapeToYamlSpec extends Specification {

  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def loader = new YamlTapeLoader(tapeRoot)

  @Shared RecordedRequest getRequest
  @Shared RecordedRequest postRequest
  @Shared RecordedResponse successResponse
  @Shared RecordedResponse failureResponse
  @Shared RecordedResponse imageResponse
  @Shared File image

  Yaml yamlReader

  void setupSpec() {
    getRequest = new RecordedRequest.Builder()
        .url("http://freeside.co/betamax")
        .addHeader(ACCEPT_LANGUAGE, "en-GB,en")
        .addHeader(IF_NONE_MATCH, "b00b135")
        .build()

    postRequest = new RecordedRequest.Builder()
        .method("POST", RequestBody.create(MediaType.parse(FORM_DATA.toString()), "q=1"))
        .url("http://github.com/")
        .build()

    successResponse = new RecordedResponse.Builder()
        .code(HTTP_OK)
        .body(ResponseBody.create(MediaType.parse("text/plain"), "O HAI!"))
        .addHeader(CONTENT_LANGUAGE, "en-GB")
        .addHeader(CONTENT_ENCODING, "none")
        .build()

    failureResponse = new RecordedResponse.Builder()
        .code(HTTP_BAD_REQUEST)
        .addHeader(CONTENT_LANGUAGE, "en-GB")
        .addHeader(CONTENT_ENCODING, "none")
        .body(ResponseBody.create(MediaType.parse("text/plain"), "KTHXBYE!"))
        .build()

    image = new File(Class.getResource("/image.png").toURI())
    imageResponse = new RecordedResponse.Builder()
        .code(HTTP_OK)
        .body(ResponseBody.create(MediaType.parse("image/png"), image.bytes))
        .build()
  }

  void setup() {
    yamlReader = new Yaml()
  }

  void "can write a tape to storage"() {
    given:
    def tape = loader.newTape("tape_loading_spec")
    tape.mode = READ_WRITE
    def writer = new StringWriter()

    when:
    tape.record(getRequest, successResponse)
    loader.writeTo(tape, writer)

    then:
    def yaml = yamlReader.loadAs(writer.toString(), Map)
    yaml.size() == 2
    yaml.name == tape.name

    yaml.interactions.size() == 1
    yaml.interactions[0].recorded instanceof Date
    yaml.interactions[0].request.method() == "GET"
    yaml.interactions[0].request.url().toString() == "http://freeside.co/betamax"
    yaml.interactions[0].response.code() == HTTP_OK
    yaml.interactions[0].response.getBodyAsText() == "O HAI!"
  }

  void "writes request headers"() {
    given:
    def tape = loader.newTape("tape_loading_spec")
    tape.mode = READ_WRITE
    def writer = new StringWriter()

    when:
    tape.record(getRequest, successResponse)
    loader.writeTo(tape, writer)

    then:
    def yaml = yamlReader.loadAs(writer.toString(), Map)
    yaml.interactions[0].request.header(ACCEPT_LANGUAGE) == "en-GB,en"
    yaml.interactions[0].request.header(IF_NONE_MATCH) == "b00b135"
  }

  void "writes response headers"() {
    given:
    def tape = loader.newTape("tape_loading_spec")
    tape.mode = READ_WRITE
    def writer = new StringWriter()

    when:
    tape.record(getRequest, successResponse)
    loader.writeTo(tape, writer)

    then:
    def yaml = yamlReader.loadAs(writer.toString(), Map)
    yaml.interactions[0].response.header(CONTENT_TYPE) == "text/plain; charset=utf-8"
    yaml.interactions[0].response.header(CONTENT_LANGUAGE) == "en-GB"
    yaml.interactions[0].response.header(CONTENT_ENCODING) == "none"
  }

  void "can write requests with a body"() {
    given:
    def tape = loader.newTape("tape_loading_spec")
    tape.mode = READ_WRITE
    def writer = new StringWriter()

    when:
    tape.record(postRequest, successResponse)
    loader.writeTo(tape, writer)

    then:
    println(writer.toString())
    def yaml = yamlReader.loadAs(writer.toString(), Map)
    yaml.interactions[0].request.method() == "POST"
    yaml.interactions[0].request.getBodyAsText() == "q=1"
  }

  void "can write multiple interactions"() {
    given:
    def tape = loader.newTape("tape_loading_spec")
    tape.mode = READ_WRITE
    def writer = new StringWriter()

    when:
    tape.record(getRequest, successResponse)
    tape.record(postRequest, failureResponse)
    loader.writeTo(tape, writer)

    then:
    def yaml = yamlReader.loadAs(writer.toString(), Map)
    yaml.interactions.size() == 2
    yaml.interactions[0].request.method() == "GET"
    yaml.interactions[1].request.method == "POST"
    yaml.interactions[0].response.code() == HTTP_OK
    yaml.interactions[1].response.code() == HTTP_BAD_REQUEST
  }

  void "can write a binary RecordedResponse body"() {
    given:
    def tape = loader.newTape("tape_loading_spec")
    tape.mode = READ_WRITE
    def writer = new StringWriter()

    when:
    tape.record(getRequest, imageResponse)
    loader.writeTo(tape, writer)

    then:
    def yaml = yamlReader.loadAs(writer.toString(), Map)
    yaml.interactions[0].response.header(CONTENT_TYPE) == "image/png"
    yaml.interactions[0].response.getBody() == image.bytes
  }

  void "text RecordedResponse body is written to file as plain text"() {
    given:
    def tape = loader.newTape("tape_loading_spec")
    tape.mode = READ_WRITE
    def writer = new StringWriter()

    when:
    tape.record(getRequest, successResponse)
    loader.writeTo(tape, writer)

    then:
    def yaml = yamlReader.loadAs(writer.toString(), Map)
    yaml.interactions[0].response.header(CONTENT_TYPE) == "text/plain; charset=utf-8"
    yaml.interactions[0].response.getBodyAsText() == 'O HAI!'
  }

  void "binary RecordedResponse body is written to file as binary data"() {
    given:
    def tape = loader.newTape("tape_loading_spec")
    tape.mode = READ_WRITE
    def writer = new StringWriter()

    when:
    tape.record(getRequest, imageResponse)
    loader.writeTo(tape, writer)

    then:
    writer.toString().contains("!!binary")
  }

}
