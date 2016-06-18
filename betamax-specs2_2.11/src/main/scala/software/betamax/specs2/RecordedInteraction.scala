package software.betamax.specs2

import software.betamax.{ProxyConfiguration, Recorder, _}

/**
  * Created by sean on 2/11/16.
  */

object RecordedInteraction {
  def apply[T](tape: String, configuration: ProxyConfigurationBuilder[ProxyConfiguration.Builder] => ProxyConfigurationBuilder[ProxyConfiguration.Builder] = builder => builder)(block: => T): T = {
    val recorder = new Recorder(configuration(ProxyConfiguration.builder()).build())
    recorder.start(tape)

    try {
      block
    } finally {
      recorder.stop()
    }
  }
}