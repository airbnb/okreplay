package walkman

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static walkman.TapeMode.READ_ONLY
import static walkman.TapeMode.READ_WRITE

/**
 * Testing a custom matcher when being used in the proxy.
 */
@Unroll
class CustomMatcherSpec extends Specification {
  @Shared def tapeRoot = new File(CustomMatcherSpec.class.getResource("/walkman/tapes/").toURI())

  static def simplePost(OkHttpClient client, String url, String payload) {
    def request = new Request.Builder()
        .method("POST", RequestBody.create(MediaType.parse('text/plain'), payload))
        .url(url)
        .build()
    return client.newCall(request).execute()
  }

  void "Using a custom matcher it should replay"() {
    given:
    def imr = new InstrumentedMatchRule()
    def configuration = new WalkmanConfig.Builder()
        .sslEnabled(true)
        .tapeRoot(tapeRoot)
        .defaultMode(READ_ONLY)
        .defaultMatchRule(imr)
        .interceptor(new WalkmanInterceptor())
        .build()
    def recorder = new Recorder(configuration)
    def client = new OkHttpClient.Builder()
        .addInterceptor(configuration.interceptor())
        .build()
    recorder.start("httpBinTape")
    imr.requestValidations << { r ->
      //Will run this request validation on both requests being matched
      //No matter what, either recorded, or sent, I should have a payload of "BUTTS"
      //I'm posting "BUTTS" and the recorded interaction should have "BUTTS"
      assert r.hasBody()
    }

    when:
    def response = simplePost(client, "https://httpbin.org/post", "BUTTS")

    then:
    def content = response.body().string()

    content == "Hey look some text: BUTTS"

    cleanup:
    recorder.stop()
  }

  void "Using a custom matcher it should record a new one"() {
    given:
    def tapeRoot = Files.createTempDir() //Using a temp dir this time
    def imr = new InstrumentedMatchRule()
    def configuration = new WalkmanConfig.Builder()
        .sslEnabled(true)
        .tapeRoot(tapeRoot)
        .defaultMode(READ_WRITE)
        .defaultMatchRule(imr)
        .interceptor(new WalkmanInterceptor())
        .build()
    def recorder = new Recorder(configuration)
    def client = new OkHttpClient.Builder()
        .addInterceptor(configuration.interceptor())
        .build()
    recorder.start("httpBinTape")

    when:
    def response = simplePost(client, "https://httpbin.org/post", "LOLWUT")

    then:
    response.toString()
    recorder.stop()
    // The tape is written when it's referenced not in this dir
    // make sure there's a file in there
    def recordedTape = new File(tapeRoot, "httpBinTape.yaml")
    // It should have recorded it to the tape
    recordedTape.exists()
  }
}
