package okreplay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import static okreplay.Util.newReader;
import static okreplay.Util.newWriter;

public class DefaultTapeRoot implements TapeRoot {
  private static final String FILE_CHARSET = "UTF-8";
  protected final File root;

  public DefaultTapeRoot(File root) {
    this.root = root;
  }

  @Override public Reader readerFor(String tapeFileName) {
    File file = new File(root, tapeFileName);
    try {
      return newReader(file, Charset.forName(FILE_CHARSET));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public Writer writerFor(String tapePath) {
    File file = new File(root, tapePath);
    //noinspection ResultOfMethodCallIgnored
    file.getParentFile().mkdirs();
    try {
      return newWriter(file, Charset.forName(FILE_CHARSET));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public boolean tapeExists(String tapeFileName) {
    return new File(root, tapeFileName).isFile();
  }

  @Override public File get() {
    return root;
  }
}
