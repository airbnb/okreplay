package okreplay

import com.google.common.io.Files
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.*

import static java.net.HttpURLConnection.HTTP_OK
import static okreplay.TapeMode.*

@Unroll
abstract class TapeModeSpec extends Specification {

  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared Recorder recorder = new Recorder(configuration)
  @Shared @AutoCleanup("stop") def endpoint = new MockWebServer()

  protected abstract OkReplayConfig getConfiguration()
  protected abstract void makeRequest()

  def client = new OkHttpClient.Builder()
      .addInterceptor(new OkReplayInterceptor())
      .build()

  void setupSpec() {
    endpoint.start()
    endpoint.enqueue(new MockResponse().setBody("Hello World!"))
  }

  void cleanup() {
    recorder.stop()
  }

  void "in #mode mode the proxy rejects a request if no recorded interaction exists"() {
    given: "a read-only tape is inserted"
    recorder.start("read only tape", mode)

    when: "a request is made that does not match anything recorded on the tape"
    makeRequest()

    then: "the proxy rejects the request"
    thrown NonWritableTapeException

    where:
    mode << [READ_ONLY, READ_SEQUENTIAL]
  }

  void "in #mode mode a new interaction is recorded"() {
    given: "an empty write-only tape is inserted"
    new File(tapeRoot, "blank_tape_${mode}.yaml").delete()
    recorder.start("blank tape $mode", mode)
    def tape = recorder.tape

    when: "a request is made"
    makeRequest()

    then: "the interaction is recorded"
    tape.size() == old(tape.size()) + 1

    where:
    mode << [READ_WRITE, WRITE_ONLY, WRITE_SEQUENTIAL]
  }

  void "in write-only mode the proxy overwrites an existing matching interaction"() {
    given: "an existing tape file is inserted in write-only mode"
    def tapeFile = new File(tapeRoot, "write_only_tape.yaml")
    tapeFile.text = """\
!tape
name: write only tape
interactions:
- recorded: 2011-08-26T21:46:52.000Z
  request:
    method: GET
    url: ${endpoint.url("/").toString()}
    headers: {}
  response:
    status: 202
    headers: {}
    body: Previous response made when endpoint was down.
"""
    recorder.start("write only tape", WRITE_ONLY)
    def tape = recorder.tape as MemoryTape

    when: "a request is made that matches a request already recorded on the tape"
    makeRequest()

    then: "the previously recorded request is overwritten"
    tape.size() == old(tape.size())
    tape.interactions[-1].response.code() == HTTP_OK
    tape.interactions[-1].response.body()
  }

  @Issue([
      "https://github.com/robfletcher/betamax/issues/7",
      "https://github.com/robfletcher/betamax/pull/70"
  ])
  void "in write-sequential mode the proxy records additional interactions"() {
    given: "an existing tape file is inserted in write-sequential mode"
    def tapeFile = new File(tapeRoot, "write_sequential_tape.yaml")
    tapeFile.text = """\
!tape
name: write sequential tape
interactions:
- recorded: 2011-08-26T21:46:52.000Z
  request:
    method: GET
    url: ${endpoint.url("/").toString()}
    headers: {}
  response:
    status: 202
    headers: {}
    body: Previous response made when endpoint was down.
"""
    recorder.start("write sequential tape", WRITE_SEQUENTIAL)
    def tape = recorder.tape as MemoryTape

    when: "a request is made that matches a request already recorded on the tape"
    makeRequest()

    then: "the previously recorded request is overwritten"
    tape.size() == old(tape.size()) + 1
    tape.interactions[-1].response.code() == HTTP_OK
    tape.interactions[-1].response.body()
  }
}
