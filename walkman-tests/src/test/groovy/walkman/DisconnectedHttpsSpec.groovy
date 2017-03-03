package walkman

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Rule
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification

import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK
import static walkman.Headers.X_WALKMAN

@Ignore
@Issue("https://github.com/robfletcher/betamax/issues/117")
class DisconnectedHttpsSpec extends Specification {
  static final TAPE_ROOT = new File(DisconnectedHttpsSpec.getResource("/walkman/tapes").toURI())
  def configuration = new WalkmanConfig.Builder().tapeRoot(TAPE_ROOT).sslEnabled(true).build()
  def interceptor = new WalkmanInterceptor()
  @Rule RecorderRule recorder = new RecorderRule(configuration, interceptor)
  def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  @Walkman(tape = "disconnected https spec")
  void "can play back a recorded HTTPS response without contacting the original server"() {
    when:
    def request = new Request.Builder().url("https://freeside.bv/").build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "Walkman"
    response.header(X_WALKMAN) == "PLAY"
    response.body().string() == "O HAI!"
  }
}
