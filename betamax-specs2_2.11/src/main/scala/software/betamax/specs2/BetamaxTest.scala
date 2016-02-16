package software.betamax.specs2

import software.betamax._
import TapeMode._
import org.specs2.specification.{Scope, After}
import software.betamax.{Recorder, ProxyConfiguration, MatchRules, TapeMode}

/**
  * Created by sean on 2/11/16.
  */

object RecordedInteraction {
  def apply[T](tape: String, tapeMode: TapeMode = READ_ONLY, matchRules: Seq[MatchRule] = Seq(MatchRules.method, MatchRules.uri))(block: => T): T = {
    val config: ProxyConfiguration = ProxyConfiguration.builder().defaultMode(tapeMode).defaultMatchRules(matchRules: _*).build()
    val recorder = new Recorder(config)
    recorder.start(tape)

    try {
      block
    } finally {
      recorder.stop()
    }
  }
}