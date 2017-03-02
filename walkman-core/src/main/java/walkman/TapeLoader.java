package walkman;

import java.io.File;

/**
 * The interface for factories that load tapes from file storage.
 */
public interface TapeLoader<T extends Tape> {
  /**
   * Loads the named tape or returns a new blank tape if an existing tape cannot be located.
   *
   * @param name the name of the tape.
   * @return a tape loaded from a file or a new blank tape.
   */
  T loadTape(String name);

  void writeTape(Tape tape);

  /**
   * @return an appropriate file for storing a tape with the supplied name.
   */
  File fileFor(String tapeName);
}
