package okreplay

import java.util.ArrayList

/**
 * This class is the main interface to OkReplay. It controls the OkReplay lifecycle, inserting and
 * ejecting [Tape] instances and starting and stopping recording sessions.
 */
open class Recorder(private val configuration: OkReplayConfig) {
  private val listeners = ArrayList<RecorderListener>()

  /**
   * The current active _tape_.
   */
  var tape: Tape? = null
    private set

  /** Not just a property as `tapeRoot` gets changed during constructor.  */
  private val tapeLoader: TapeLoader<out Tape>
    get() = YamlTapeLoader(configuration.tapeRoot)

  init {
    configuration.registerListeners(listeners)
  }

  /**
   * Starts the Recorder, inserting a tape with the specified parameters.
   *
   * @param tapeName  the name of the tape.
   * @param mode      the tape mode. If not supplied the default mode from the configuration is
   * used.
   * @param matchRule the rules used to match recordings on the tape. If not supplied a default is
   * used.
   * @throws IllegalStateException if the Recorder is already started.
   */
  fun start(tapeName: String, mode: TapeMode?, matchRule: MatchRule?) {
    check(tape == null) { "start called when Recorder is already started" }
    tape = tapeLoader.loadTape(tapeName)
    tape!!.mode = mode ?: configuration.defaultMode
    tape!!.matchRule = matchRule ?: configuration.defaultMatchRule
    configuration.interceptor().start(configuration, tape!!)

    for (listener in listeners) {
      listener.onRecorderStart(tape)
    }
  }

  fun start(tapeName: String, mode: TapeMode) {
    start(tapeName, mode.toNullable(), null)
  }

  fun start(tapeName: String) {
    start(tapeName, null, null)
  }

  /**
   * Stops the Recorder and writes its current tape out to a file.
   *
   * @throws IllegalStateException if the Recorder is not started.
   */
  fun stop() {
    checkNotNull(tape) { "stop called when Recorder is not started" }

    for (listener in listeners) {
      listener.onRecorderStop()
    }
    tapeLoader.writeTape(tape)
    tape = null
  }
}
