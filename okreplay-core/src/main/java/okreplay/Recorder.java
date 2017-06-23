package okreplay;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class is the main interface to OkReplay. It controls the OkReplay lifecycle, inserting and
 * ejecting {@link Tape} instances and starting and stopping recording sessions.
 */
public class Recorder {
  private final OkReplayConfig configuration;
  private final Collection<RecorderListener> listeners = new ArrayList<>();
  private Tape tape;

  public Recorder(OkReplayConfig configuration) {
    this.configuration = configuration;
    configuration.registerListeners(listeners);
  }

  /**
   * Starts the Recorder, inserting a tape with the specified parameters.
   *
   * @param tapeName  the name of the tape.
   * @param mode      the tape mode. If not supplied the default mode from the configuration is
   *                  used.
   * @param matchRule the rules used to match recordings on the tape. If not supplied a default is
   *                  used.
   * @throws IllegalStateException if the Recorder is already started.
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public void start(String tapeName, Optional<TapeMode> mode, Optional<MatchRule> matchRule) {
    if (tape != null) {
      throw new IllegalStateException("start called when Recorder is already started");
    }
    tape = getTapeLoader().loadTape(tapeName);
    tape.setMode(mode.or(configuration.getDefaultMode()));
    tape.setMatchRule(matchRule.or(configuration.getDefaultMatchRule()));
    configuration.interceptor().start(configuration, tape);

    for (RecorderListener listener : listeners) {
      listener.onRecorderStart(tape);
    }
  }

  public void start(String tapeName, TapeMode mode) {
    start(tapeName, mode.toOptional(), Optional.<MatchRule>absent());
  }

  public void start(String tapeName) {
    start(tapeName, Optional.<TapeMode>absent(), Optional.<MatchRule>absent());
  }

  /**
   * Stops the Recorder and writes its current tape out to a file.
   *
   * @throws IllegalStateException if the Recorder is not started.
   */
  public void stop() {
    if (tape == null) {
      throw new IllegalStateException("stop called when Recorder is not started");
    }

    for (RecorderListener listener : listeners) {
      listener.onRecorderStop();
    }
    getTapeLoader().writeTape(tape);
    tape = null;
  }

  /**
   * Gets the current active _tape_.
   *
   * @return the active _tape_.
   */
  public Tape getTape() {
    return tape;
  }

  /** Not just a property as `tapeRoot` gets changed during constructor. */
  private TapeLoader<? extends Tape> getTapeLoader() {
    return new YamlTapeLoader(configuration.getTapeRoot());
  }
}
