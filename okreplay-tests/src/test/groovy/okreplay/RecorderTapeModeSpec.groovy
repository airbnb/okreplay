package okreplay

import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

import static okreplay.TapeMode.READ_ONLY
import static okreplay.TapeMode.READ_WRITE

@Issue("https://github.com/robfletcher/betamax/issues/106")
@Unroll
class RecorderTapeModeSpec extends Specification {
  void "tape mode is #expectedMode if default mode is #defaultMode and start is called with #modeParam"() {
    given:
    def configuration = new OkReplayConfig.Builder()
        .defaultMode(defaultMode)
        .interceptor(new OkReplayInterceptor())
        .build()
    def recorder = new Recorder(configuration)

    when:
    recorder.start("recorder tape mode spec", modeParam)

    then:
    recorder.tape.mode == expectedMode

    where:
    defaultMode | modeParam  | expectedMode
    READ_ONLY   | READ_WRITE | READ_WRITE
  }
}
