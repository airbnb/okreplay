package software.betamax.specs2

import software.betamax.{Recorder, _}

/**
  * Created by sean on 2/11/16.
  */

object RecordedInteraction {
  def apply[T](tape: String, configuration: ConfigurationBuilder => ConfigurationBuilder = builder => builder)(block: => T): T = {
    val recorder = new Recorder(configuration(Configuration.builder()).build())
    recorder.start(tape)

    try {
      block
    } finally {
      recorder.stop()
    }
  }
}