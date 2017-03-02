package walkman

import com.google.common.base.Charsets
import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.*

import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK
import static walkman.Headers.X_WALKMAN
import static walkman.TapeMode.READ_WRITE

@Stepwise
@Timeout(10)
class ProxyRecordAndPlaybackSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def configuration = Configuration.builder()
      .tapeRoot(tapeRoot)
      .defaultMode(READ_WRITE)
      .build()
  @Shared def interceptor = new WalkmanInterceptor();
  @Shared def recorder = new Recorder(configuration, interceptor)
  @Shared def endpoint = new MockWebServer()
  @Shared def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  void setupSpec() {
    recorder.start("proxy record and playback spec")
  }

  void cleanupSpec() {
    try {
      recorder.stop()
    } catch (IllegalStateException ignored) {
      // recorder was already stopped
    }
  }

  void "proxy makes a real HTTP request the first time it gets a request for a URI"() {
    given:
    endpoint.start()
    endpoint.enqueue(new MockResponse().setBody("Hello World"))

    when:
    def request = new Request.Builder()
        .url(endpoint.url("/"))
        .build()

    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Walkman"
    response.header(X_WALKMAN) == "REC"
    response.body().string() == "Hello World"

    and:
    recorder.tape.size() == 1
    endpoint.getRequestCount() == 1
  }

  void "subsequent requests for the same URI are played back from tape"() {
    when:
    def request = new Request.Builder()
        .url(endpoint.url("/"))
        .build()

    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Walkman"
    response.header(X_WALKMAN) == "PLAY"
    response.body().string() == "Hello World"

    and:
    recorder.tape.size() == 1
    endpoint.getRequestCount() == 1
  }

  void "subsequent requests with a different HTTP method are recorded separately"() {
    given:
    endpoint.enqueue(new MockResponse().setBody("Hello World"))

    when:
    def request = new Request.Builder()
        .url(endpoint.url("/"))
        .method("POST",
          RequestBody.create(MediaType.parse("text/plain"), "foo".getBytes(Charsets.UTF_8)))
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Walkman"
    response.header(X_WALKMAN) == "REC"
    response.body().string() == "Hello World"

    and:
    recorder.tape.size() == 2
    recorder.tape.interactions[-1].request.method == "POST"
    endpoint.getRequestCount() == 2
  }
//
//  void "when the tape is ejected the data is written to a file"() {
//    when:
//    recorder.stop()
//
//    then:
//    def file = new File(tapeRoot, "proxy_record_and_playback_spec.yaml")
//    file.isFile()
//
//    and:
//    def yaml = file.withReader { reader ->
//      new Yaml().loadAs(reader, Map)
//    }
//    yaml.name == "proxy record and playback spec"
//    yaml.interactions.size() == 2
//  }
//
//  void "can load an existing tape from a file"() {
//    given:
//    def file = new File(tapeRoot, "existing_tape.yaml")
//    file.parentFile.mkdirs()
//    file.text = """\
//!tape
//name: existing_tape
//interactions:
//- recorded: 2011-08-19T11:45:33.000Z
//  request:
//    method: GET
//    url: http://icanhascheezburger.com/
//    headers: {Accept-Language: "en-GB,en", If-None-Match: b00b135}
//  response:
//    status: 200
//    headers: {Content-Type: text/plain, Content-Language: en-GB}
//    body: O HAI!
//"""
//
//    when:
//    recorder.start("existing_tape")
//
//    then:
//    recorder.tape.name == "existing_tape"
//    recorder.tape.size() == 1
//  }
//
//  void "can play back a loaded tape"() {
//    when:
//    HttpURLConnection connection = "http://icanhascheezburger.com/".toURL().openConnection()
//
//    then:
//    connection.responseCode == HTTP_OK
//    connection.getHeaderField(VIA) == "Walkman"
//    connection.getHeaderField(X_WALKMAN) == "PLAY"
//    connection.inputStream.text == "O HAI!"
//  }
}
