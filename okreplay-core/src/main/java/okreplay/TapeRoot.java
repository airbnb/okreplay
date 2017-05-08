package okreplay;

import java.io.File;
import java.io.Reader;
import java.io.Writer;

@SuppressWarnings("WeakerAccess")
public interface TapeRoot {
  /** Returns a reader for reading a tape in the provided path. Throws if the file doesnt exist. */
  Reader readerFor(String tapeFileName);
  /** Returns a writer for writing to a new tape in the provided path. */
  Writer writerFor(String tapePath);
  /** Return whether a tape file in the provided path already exists. */
  boolean tapeExists(String tapeFileName);
  /** Returns the root directory. */
  File get();
}
