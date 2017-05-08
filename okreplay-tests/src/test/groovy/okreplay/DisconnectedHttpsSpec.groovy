package okreplay

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Rule
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification

import static com.google.common.net.HttpHeaders.VIA
import static java.net.HttpURLConnection.HTTP_OK
import static okreplay.Headers.X_OKREPLAY

@Ignore
@Issue("https://github.com/robfletcher/betamax/issues/117")
class DisconnectedHttpsSpec extends Specification {
  static final TAPE_ROOT = new File(DisconnectedHttpsSpec.getResource("/okreplay/tapes").toURI())
  def configuration = new OkReplayConfig.Builder().tapeRoot(TAPE_ROOT).sslEnabled(true).build()
  def interceptor = new OkReplayInterceptor()
  @Rule RecorderRule recorder = new RecorderRule(configuration, interceptor)
  def client = new OkHttpClient.Builder()
      .addInterceptor(interceptor)
      .build()

  @OkReplay(tape = "disconnected https spec")
  void "can play back a recorded HTTPS response without contacting the original server"() {
    when:
    def request = new Request.Builder().url("https://freeside.bv/").build()
    def response = client.newCall(request).execute()

    then:
    response.code() == HTTP_OK
    response.header(VIA) == "OkReplay"
    response.header(X_OKREPLAY) == "PLAY"
    response.body().string() == "O HAI!"
  }
}
