package com.gneoxsolutions.betamax.specs2

import com.gneoxsolutions.betamax.TapeMode._
import com.gneoxsolutions.betamax._
import org.specs2.specification.{Scope, After}

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