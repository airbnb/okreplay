package walkman

import okhttp3.OkHttpClient
import okhttp3.Request
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

@Issue("https://github.com/robfletcher/betamax/issues/18")
class NoTapeSpec extends Specification {
  @Shared def configuration = new WalkmanConfig.Builder().build()
  @Shared def interceptor = new WalkmanInterceptor()
  @Shared def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()
  @Shared def recorder = new Recorder(configuration)
  @Shared @AutoCleanup("stop") def proxy = new ProxyServer(configuration, interceptor)

  void setupSpec() {
    proxy.start(null)
  }

  void "an error is returned if the proxy intercepts a request when no tape is inserted"() {
    when:
    def request = new Request.Builder()
        .url("http://localhost")
        .build()
    def response = client.newCall(request).execute()

    then:
    response.code() == 403
    response.body().string() == "No tape"
  }
}
