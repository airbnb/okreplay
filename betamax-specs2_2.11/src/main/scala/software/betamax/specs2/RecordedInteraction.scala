package software.betamax.specs2

import java.io.File

import software.betamax.TapeMode._
import software.betamax.{MatchRules, ProxyConfiguration, Recorder, TapeMode, _}

/**
  * Created by sean on 2/11/16.
  */

object RecordedInteraction {
  def apply[T](tape: String,
               tapeMode: TapeMode = READ_ONLY,
               matchRules: Seq[MatchRule] = Seq(MatchRules.method, MatchRules.uri),
               sslEnabled: Boolean = true,
               tapeRoot: String = Configuration.DEFAULT_TAPE_ROOT)(block: => T): T = {
    val recorder = new Recorder({
      ProxyConfiguration.builder()
        .defaultMode(tapeMode)
        .defaultMatchRules(matchRules: _*)
        .sslEnabled(sslEnabled)
        .tapeRoot(new File(tapeRoot))
        .build()
    })
    recorder.start(tape)

    try {
      block
    } finally {
      recorder.stop()
    }
  }
}