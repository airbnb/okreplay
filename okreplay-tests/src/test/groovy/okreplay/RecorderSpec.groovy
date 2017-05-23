package okreplay

import spock.lang.Specification
import spock.lang.Subject

class RecorderSpec extends Specification {
  def listener = Mock(RecorderListener)
  def configuration = Spy(OkReplayConfig, constructorArgs: [new OkReplayConfig.Builder()
      .interceptor(new OkReplayInterceptor())]) {
    registerListeners(_) >> { it[0] << listener }
  }
  @Subject def recorder = new Recorder(configuration)

  void "throws an exception if started when already running"() {
    given:
    recorder.start("a tape")

    when:
    recorder.start("another tape")

    then:
    thrown IllegalStateException
  }

  void "throws an exception if stopped when not started"() {
    when:
    recorder.stop()

    then:
    thrown IllegalStateException
  }

  void "calls listeners on start"() {
    when:
    recorder.start("a tape")

    then:
    1 * listener.onRecorderStart(_ as Tape)
  }

  void "calls listeners on stop"() {
    given:
    recorder.start("a tape")

    when:
    recorder.stop()

    then:
    1 * listener.onRecorderStop()
  }

}
