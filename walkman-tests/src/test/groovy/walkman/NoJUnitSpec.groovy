package walkman

import com.google.common.io.Files
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.*

import static Headers.X_WALKMAN
import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK

@Issue("https://github.com/robfletcher/betamax/issues/107")
@Unroll
@Timeout(10)
class NoJUnitSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def configuration = new WalkmanConfig.Builder()
      .tapeRoot(tapeRoot)
      .sslEnabled(true)
      .interceptor(new WalkmanInterceptor())
      .build()
  @Shared Recorder recorder = new Recorder(configuration)

  @Shared def httpEndpoint = new MockWebServer()

  def client = new OkHttpClient.Builder()
      .addInterceptor(configuration.interceptor())
      .build()

  void setupSpec() {
    httpEndpoint.start()
    httpEndpoint.enqueue(new MockResponse().setBody("Hello World!"))
  }

  void setup() {
    recorder.start("no junit spec", TapeMode.READ_WRITE)
  }

  void cleanup() {
    recorder.stop()
  }

  void "proxy intercepts #scheme URL connections"() {
    given:
    def request = new Request.Builder().url(url).build()
    def response = client.newCall(request).execute()

    expect:
    response.code() == HTTP_OK
    response.header(VIA) == "Walkman"
    response.header(X_WALKMAN) == "REC"
    response.body().string() == "Hello World!"

    where:
    url << [httpEndpoint.url("/")]
  }
}
