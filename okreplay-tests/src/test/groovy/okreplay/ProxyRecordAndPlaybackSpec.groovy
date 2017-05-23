package okreplay

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
import static okreplay.Headers.X_OKREPLAY
import static okreplay.TapeMode.READ_WRITE

@Stepwise
@Timeout(10)
class ProxyRecordAndPlaybackSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def configuration = new OkReplayConfig.Builder()
      .tapeRoot(tapeRoot)
      .defaultMode(READ_WRITE)
      .interceptor(new OkReplayInterceptor())
      .build()
  @Shared def recorder = new Recorder(configuration)
  @Shared def endpoint = new MockWebServer()
  @Shared def client = new OkHttpClient.Builder()
      .addInterceptor(configuration.interceptor())
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
    response.header(VIA) == "OkReplay"
    response.header(X_OKREPLAY) == "REC"
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
    response.header(VIA) == "OkReplay"
    response.header(X_OKREPLAY) == "PLAY"
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
    response.header(VIA) == "OkReplay"
    response.header(X_OKREPLAY) == "REC"
    response.body().string() == "Hello World"

    and:
    recorder.tape.size() == 2
    recorder.tape.interactions[-1].request.method == "POST"
    endpoint.getRequestCount() == 2
  }
}
