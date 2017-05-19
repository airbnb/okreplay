package okreplay

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static java.net.HttpURLConnection.HTTP_OK
import static okreplay.TapeMode.READ_WRITE

class ContentTypeSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def loader = new YamlTapeLoader(tapeRoot)
  @Shared Tape tape = loader.loadTape('tape_spec')
  @Shared File image = new File(Class.getResource("/image.png").toURI())

  @Shared RecordedResponse successResponse = new RecordedResponse.Builder()
      .code(HTTP_OK)
      .body(ResponseBody.create(MediaType.parse("text/plain"), "OK"))
      .build()

  void setup() {
    tape.mode = READ_WRITE
  }

  void 'can record post requests with an image content-type'() {
    given: 'a request with some content'
    def imagePostRequest = new RecordedRequest.Builder()
        .method("POST", RequestBody.create(MediaType.parse("image/png"), image.bytes))
        .url("http://github.com/")
        .build()

    when: 'the request and its response are recorded'
    tape.record(imagePostRequest, successResponse)

    then: 'the request body is stored on the tape'
    def interaction = tape.interactions[-1]
    interaction.request.body() == imagePostRequest.body()
  }
}
