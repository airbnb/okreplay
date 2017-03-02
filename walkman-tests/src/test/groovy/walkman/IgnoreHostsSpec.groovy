package walkman

import com.google.common.io.Files
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.*

import static com.google.common.net.HttpHeaders.VIA
import static walkman.TapeMode.READ_WRITE

@Issue("https://github.com/robfletcher/betamax/issues/16")
@Unroll
class IgnoreHostsSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") File tapeRoot = Files.createTempDir()
  def configuration = Spy(Configuration, constructorArgs: [Configuration.builder()
      .tapeRoot(tapeRoot)
      .defaultMode(READ_WRITE)])
  def interceptor = new WalkmanInterceptor()
  @AutoCleanup("stop") Recorder recorder = new Recorder(configuration, interceptor)
  @Shared MockWebServer endpoint = new MockWebServer()

  def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  void setupSpec() {
    endpoint.start()
  }

  void "does not proxy a request to #requestURI when ignoring #ignoreHosts"() {
    given: "proxy is configured to ignore local connections"
    configuration.getIgnoreHosts() >> [ignoreHosts]
    recorder.start("ignore hosts spec")

    when: "a request is made"
    endpoint.enqueue(new MockResponse().setBody("OK"))
    def request = new Request.Builder()
        .url(requestURI)
        .build()
    def response = client.newCall(request).execute()

    then: "the request is not intercepted by the proxy"
    println(configuration.getIgnoreHosts())
    response.header(VIA) == null

    and: "nothing is recorded to the tape"
    recorder.tape.size() == old(recorder.tape.size())

    where:
    ignoreHosts              | requestURI
    endpoint.url("/").host() | endpoint.url("/").toString()
    "localhost"              | "http://localhost:${endpoint.url("/").port()}"
    "127.0.0.1"              | "http://127.0.0.1:${endpoint.url("/").port()}"
    endpoint.url("/").host() | "http://localhost:${endpoint.url("/").port()}"
  }

  void "does not proxy a request to #requestURI when ignoreLocalhost is true"() {
    given: "proxy is configured to ignore local connections"
    configuration.ignoreLocalhost >> true
    recorder.start("ignore hosts spec")

    when: "a request is made"
    endpoint.enqueue(new MockResponse().setBody("OK"))
    def request = new Request.Builder()
        .url(requestURI)
        .build()
    def response = client.newCall(request).execute()

    then: "the request is not intercepted by the proxy"
    response.header(VIA) == null

    and: "nothing is recorded to the tape"
    recorder.tape.size() == old(recorder.tape.size())

    where:
    requestURI << [endpoint.url('/').toString(),
        "http://localhost:${endpoint.url('/').port()}"]
  }
}
